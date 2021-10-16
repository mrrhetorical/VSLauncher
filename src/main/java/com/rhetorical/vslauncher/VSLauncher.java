package com.rhetorical.vslauncher;

import com.google.gson.Gson;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class VSLauncher {

	private static VSLauncherPrefs vsLauncherPrefs;
	private static File installDir;

	private static Map<String, Modpack> modpackMap = new HashMap<>();

	public static void main( String[] args) throws Exception {
		File preferences = new File("vslauncherprefs.json");

		if (!preferences.exists()) {
			if (!preferences.createNewFile()) {
				throw new IOException("Could not create preferences file!");
			}

			vsLauncherPrefs = new VSLauncherPrefs();
		} else {

			Gson gson = new Gson();

			Reader reader = Files.newBufferedReader(preferences.toPath());

			vsLauncherPrefs = gson.fromJson(reader, VSLauncherPrefs.class);
		}

		installDir = findInstallDirectory();

		if (installDir != null) {
			System.out.println(String.format("Game install found at %s", installDir.toString()));
		} else {
			throw new FileNotFoundException("No game install found!");
		}


		updateModpackInfo();

		processInput();
	}

	private static void processInput() {
		Scanner scanner = new Scanner(System.in);



		scanner.close();
	}

	private static void updateModpackInfo() throws IOException {

		if (vsLauncherPrefs.modpacks != null) {
			for (String str : vsLauncherPrefs.modpacks) {
				URLConnection connection = new URL(str).openConnection();

				Scanner scanner = new Scanner(connection.getInputStream());

				String response = scanner.useDelimiter("\\A").next();
				scanner.close();

				System.out.println(String.format("Requested data from %s and received %s", str, response));
			}
		}

	}

	private static File findInstallDirectory() {

		File file = new File(System.getenv("APPDATA"), "\\VintagestoryData\\Mods");

		if (!file.exists()) {
			System.out.println("WARNING: Could not find install of Vintage Story. Please run the game at least once.");
			return null;
		}

		return file;
	}
}
