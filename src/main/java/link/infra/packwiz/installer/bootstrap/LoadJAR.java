package link.infra.packwiz.installer.bootstrap;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.cli.Options;

public class LoadJAR {
	
	private static Class<?> mainClass = null;
	
	private static void loadClass(String path) throws MalformedURLException, ClassNotFoundException {
		if (mainClass != null) {
			return;
		}
		
		if (path == null) {
			path = "./packwiz-installer.jar";
		}
		
		URLClassLoader child = new URLClassLoader(new URL[] { new File(path).toURI().toURL() },
				LoadJAR.class.getClassLoader());
		mainClass = Class.forName("link.infra.packwiz.installer.Main", true, child);
	}
	
	public static boolean addOptions(Options options, String path) {
		try {
			loadClass(path);
			
			Method method = mainClass.getDeclaredMethod("addNonBootstrapOptions", Options.class);
			method.invoke(null, options);
		} catch (Exception e) {
			// Woah that was a lot of errors!
			// Just ignore them, it doesn't matter if there aren't any more options added anyway - it's just a help command.
			return false;
		}
		return true;
	}
	
	public static void start(String[] args, String path) throws ClassNotFoundException, Exception {
		loadClass(path);
		
		// Must be casted to Object (not array) because varargs
		mainClass.getConstructor(String[].class).newInstance((Object)args);
	}
}
