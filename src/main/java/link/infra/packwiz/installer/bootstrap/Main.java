package link.infra.packwiz.installer.bootstrap;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

	private static final String DEFAULT_UPDATE_URL = "https://api.github.com/repos/comp500/packwiz-installer/releases/latest";
	public static final String JAR_NAME = "packwiz-installer.jar";

	private String updateURL = DEFAULT_UPDATE_URL;
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
				System.exit(1);
			} catch (Exception e) {
				showError(e, "There was an error loading packwiz-installer:");
				System.exit(1);
			}
			return;
		}

		try {
			doUpdate();
		} catch (Exception e) {
			showError(e, "There was an error downloading packwiz-installer:");
		}

		try {
			LoadJAR.start(args, jarPath);
		} catch (Exception e) {
			showError(e, "There was an error loading packwiz-installer (did it download properly?):");
			System.exit(1);
		}
	}

	private void doUpdate() throws IOException, GithubException {
		String currVersion = LoadJAR.getVersion(jarPath);
		Release ghRelease = requestRelease();

		System.out.println("Current version is: " + currVersion);
		System.out.println("New version is: " + ghRelease.tagName);
		if (!ghRelease.tagName.equals(currVersion)) {
			System.out.println("Attempting to update...");
			RollbackHandler backup = new RollbackHandler(jarPath);

			downloadUpdate(ghRelease.downloadURL, jarPath);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				backup.rollback();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void showError(Exception e, String message) {
		if (useGUI) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, message + "\n" + e.getMessage(), "packwiz-installer-bootstrap",
					JOptionPane.ERROR_MESSAGE);
		} else {
			System.out.println(message);
			e.printStackTrace();
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
		CommandLine cmd = parser.parse(options, filterArgs(args, options));

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

	// Remove invalid arguments, because Commons CLI chokes on invalid arguments
	// (that should be passed to packwiz-installer)
	private String[] filterArgs(String[] args, Options options) {
		List<String> argsList = new ArrayList<String>(args.length);
		boolean prevOptWasArg = false;
		for (String arg : args) {
			if (arg.charAt(0) == '-' && options.hasOption(arg)) {
				if (options.getOption(arg).hasArg()) {
					prevOptWasArg = true;
				}
			} else {
				if (prevOptWasArg) {
					prevOptWasArg = false;
				} else {
					continue;
				}
			}
			argsList.add(arg);
		}

		return argsList.toArray(new String[0]);
	}

	private class Release {
		String tagName = null;
		String downloadURL = null;
	}

	private class GithubException extends Exception {
		private static final long serialVersionUID = 3843811090801607241L;

		public GithubException() {
			super("Invalid Github API response");
		}

		public GithubException(String message) {
			super("Invalid Github API response: " + message);
		}
	}

	private Release requestRelease() throws IOException, GithubException {
		Release rel = new Release();

		URL url = new URL(updateURL);
		InputStream dataStream = url.openStream();
		JsonObject object = Json.parse(new InputStreamReader(dataStream)).asObject();

		JsonValue tagName = object.get("tag_name");
		if (tagName == null || !tagName.isString()) {
			throw new GithubException("Tag name cannot be found");
		}
		rel.tagName = tagName.asString();

		JsonValue assets = object.get("assets");
		if (assets == null || !assets.isArray()) {
			throw new GithubException("Assets array cannot be found");
		}
		for (JsonValue assetValue : assets.asArray()) {
			if (!assetValue.isObject()) {
				throw new GithubException();
			}
			JsonObject asset = assetValue.asObject();
			JsonValue name = asset.get("name");
			if (name == null || !name.isString()) {
				throw new GithubException("Asset name cannot be found");
			}
			if (!name.asString().equalsIgnoreCase(JAR_NAME)) {
				continue;
			}
			JsonValue downloadURL = asset.get("browser_download_url");
			if (downloadURL == null || !downloadURL.isString()) {
				throw new GithubException("Asset Download URL cannot be found");
			}
			rel.downloadURL = downloadURL.asString();
			break;
		}
		if (rel.tagName == null) {
			throw new GithubException("Latest release asset cannot be found");
		}

		return rel;
	}

	private void downloadUpdate(String downloadURL, String path) {

	}

}
