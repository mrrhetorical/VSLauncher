package com.rhetorical.vslauncher;

import com.google.gson.Gson;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class VSLauncher {

	private static Scanner inputScanner;

	private static VSLauncherPrefs vsLauncherPrefs;
	private static File installDir;

	private static Map<String, Modpack> modpackMap = new HashMap<>();

	public static void main( String[] args) throws Exception {
		inputScanner = new Scanner(System.in);


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

		String input = "";

		while (!input.equalsIgnoreCase("Q") && !input.equalsIgnoreCase("QUIT")) {
			input = inputScanner.nextLine();

			String[] split = input.split(" ");

			if (split.length == 0) {
				continue;
			}

			if (split[0].equalsIgnoreCase("add")) {
				if (split.length != 2) {
					System.out.println("Invalid arguments! Usage: 'add [url]' to add a pack!");
				}
			}

		}


		inputScanner.close();
	}

	public void addModpack(String jsonLink) {
		Modpack modpack = new Modpack(jsonLink);



	}

	private static void updateModpackInfo() throws IOException {

		if (vsLauncherPrefs.modpacks != null) {
			for (Modpack modpack : vsLauncherPrefs.modpacks) {
				Modpack pack = getModPackFromURL(modpack.packJson);

				if (!modpack.packId.equalsIgnoreCase(pack.packId)) {
					System.out.println("Error reading in modpack! There exists a modpack with two ids at the same location!");
					continue;
				}

				if (pack.packVersion > modpack.packVersion) {
					//todo: update files
				}
			}
		}

	}

	private static Modpack getModPackFromURL(String url) throws IOException {
		URLConnection connection = new URL(url).openConnection();

		Scanner scanner = new Scanner(connection.getInputStream());

		String response = scanner.useDelimiter("\\A").next();
		scanner.close();

		System.out.println(String.format("Requested data from %s and received %s", url, response));

		Gson gson = new Gson();

		return gson.fromJson(response, Modpack.class);
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
