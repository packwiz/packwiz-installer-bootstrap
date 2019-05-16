package link.infra.packwiz.installer.bootstrap;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Main {
	
	private static final String UPDATE_URL = "https://api.github.com/repos/comp500/Demagnetize/releases/latest";
	private String updateURL = UPDATE_URL;
	private boolean skipUpdate = false;
	private boolean useGUI = true;
	private String jarPath = null;
	private UpdateWindow window = null;

	public static void main(String[] args) {
		new Main(args); // Is this bad?
	}
	
	public Main(String[] args) {
		try {
			parseOptions(args);
		} catch (ParseException e) {
			showError(e, "There was an error parsing command line arguments:");
			System.exit(1);
		}
		
		if (useGUI) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (Exception e) {
						// Ignore the exceptions, just continue using the ugly L&F
					}
					window = new UpdateWindow();
					window.frmUpdatingPackwizlauncher.setVisible(true);
				}
			});
		}
		
		if (skipUpdate) {
			try {
				LoadJAR.start(args, jarPath);
			} catch (ClassNotFoundException e) {
				showError(e, "packwiz-installer cannot be found, or there was an error loading it:");
			} catch (Exception e) {
				showError(e, "There was an error loading packwiz-installer:");
			}
			System.exit(1);
		}
		
		
		try {
			requestRelease();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			LoadJAR.start(args, jarPath);
		} catch (Exception e) {
			showError(e, "There was an error loading packwiz-installer:");
			System.exit(1);
		}
	}
	
	private void showError(Exception e, String message) {
		if (useGUI) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, message + "\n" + e.getMessage(), "packwiz-installer-bootstrap", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} else {
			System.out.println(message);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void parseOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(null, "bootstrap-update-url", true, "Github API URL for checking for updates");
		options.addOption(null, "bootstrap-no-update", false, "Don't update packwiz-installer");
		options.addOption(null, "bootstrap-main-jar", true, "Location of the packwiz-installer JAR file");
		options.addOption("g", "no-gui", false, "Don't display a GUI to show update progress");
		options.addOption("h", "help", false, "Display this message");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args, false);
		
		if (cmd.hasOption("bootstrap-main-jar")) {
			jarPath = cmd.getOptionValue("bootstrap-main-jar");
		}
		
		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			// Add options from packwiz-installer JAR, if it is present
			boolean jarLoaded = LoadJAR.addOptions(options, jarPath);
			formatter.printHelp("java -jar packwiz-installer-bootstrap.jar", options);
			if (!jarLoaded) {
				System.out.println("Options for packwiz-installer will be visible once it has been downloaded.");
			}
			System.exit(0);
		}
		
		if (cmd.hasOption("bootstrap-update-url")) {
			updateURL = cmd.getOptionValue("bootstrap-update-url");
		}
		
		if (cmd.hasOption("bootstrap-no-update")) {
			skipUpdate = true;
		}
		
		if (cmd.hasOption("no-gui")) {
			useGUI = false;
		}
	}

	private void requestRelease() throws IOException {
		URL url = new URL(updateURL);
		InputStream dataStream = url.openStream();
		JsonObject object = Json.parse(new InputStreamReader(dataStream)).asObject();
		JsonValue assets = object.get("assets");
		if (assets == null || !assets.isArray()) {
			throw new RuntimeException("Invalid Github API response");
		}
		for (JsonValue asset : assets.asArray()) {
			System.out.println(asset.toString());
		}
	}

}
