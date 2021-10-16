package com.rhetorical.vslauncher;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class VSLauncher {

	public static void main( String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);

		VSLauncherPrefs vsLauncherPrefs;

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

		File installDir = findInstallDirectory();

		if (installDir != null) {
			System.out.println(String.format("Game install found at %s", installDir.toString()));
		} else {
			throw new FileNotFoundException("No game install found!");
		}

		scanner.close();
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
