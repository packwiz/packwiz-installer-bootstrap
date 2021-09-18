package link.infra.packwiz.installer.bootstrap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main method to use for the vanilla launcher.
 * Takes configuration options as system properties, runs Main as a subprocess, then after it exits calls the configured chainload main method.
 * TODO: need to make a jar that relocates minimal-json and commons-cli?
 */
public class ChainloaderMain {
	public static void main(String[] args) {
		// TODO: parse quotes?
        List<String> bootstrapArgs = Arrays.asList(System.getProperty("packwiz.args").split(" "));

		if (bootstrapArgs.isEmpty()) {
			throw new RuntimeException("Failed to parse chainload packwiz-installer-bootstrap arguments: Please provide a class to chainload");
		}

		String chainloadClass = System.getProperty("packwiz.chainload.class");

        // TODO: run download
		runSubprocess(bootstrapArgs);
		runChainload(Arrays.asList(args), chainloadClass);
	}

	private static void runSubprocess(List<String> args) {
		List<String> command = new ArrayList<>();
		command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
		command.add("-jar");
		// Get current jar path
		try {
			command.add(Paths.get(ChainloaderMain.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
		} catch (URISyntaxException e) {
			throw new RuntimeException("packwiz-installer-bootstrap failed to run subprocess: ", e);
		}
		command.addAll(args);

		ProcessBuilder pb = new ProcessBuilder();
		pb.command(command);
		pb.inheritIO();
		try {
			int res = pb.start().waitFor();
			if (res != 0) {
				System.exit(res);
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException("packwiz-installer-bootstrap failed to run subprocess: ", e);
		}
	}

	private static void runChainload(List<String> args, String mainClassName) {
		Class<?> mainClass;
		try {
			mainClass = ChainloaderMain.class.getClassLoader().loadClass(mainClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("packwiz-installer-bootstrap failed chainload: ", e);
		}
		if (mainClass != null) {
			try {
				Method main = mainClass.getMethod("main", String[].class);
				main.invoke(null, new Object[] {args.toArray(new String[0])});
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException("packwiz-installer-bootstrap failed chainload: ", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			}
		}
	}
}
