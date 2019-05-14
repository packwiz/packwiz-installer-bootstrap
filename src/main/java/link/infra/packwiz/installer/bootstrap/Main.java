package link.infra.packwiz.installer.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

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

	public static void main(String[] args) {
		try {
			new Main().parseOptions(args);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		try {
			new Main().requestRelease();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption(null, "bootstrap-update-url", true, "Github API URL for checking for updates");
		options.addOption(null, "bootstrap-no-update", false, "Don't update packwiz-installer");
		options.addOption("g", "no-gui", false, "Don't display a GUI to show update progress");
		options.addOption("h", "help", false, "Display this message");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar packwiz-installer-bootstrap.jar", options);
			// TODO: call method on packwiz-installer to get it's options
			System.exit(0);
		}
		
		if (cmd.hasOption("bootstrap-update-url")) {
			updateURL = cmd.getOptionValue("bootstrap-update-url");
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
			
		}
	}

}
