package com.rhetorical.vslauncher;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Hello world!
 *
 */
public class VSLauncher extends Application {

	public static Stage stage;

	static VSLauncherPrefs vsLauncherPrefs;
	static File installDir;

	private static Map<String, Modpack> modpackMap = new HashMap<>();

	//default pastebin: https://pastebin.com/raw/dkn2F2N3

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;

		URL url = Paths.get("src/main/java/com/rhetorical/vslauncher/MainScene.fxml").toUri().toURL();
		Parent root = FXMLLoader.load(url);

		primaryStage.setTitle("Vintage Story Mod Launcher");
		primaryStage.setScene(new Scene(root, 320, 240));
		primaryStage.setFullScreen(false);
//		primaryStage.getIcons().add(new Image("src/main/java/com/rhetorical/vslauncher/image/launcher.jpg"));
		primaryStage.show();
	}

	public static void main(String[] args) throws Exception {
		loadPreferences();

		if (vsLauncherPrefs == null) {
			vsLauncherPrefs = new VSLauncherPrefs();
		}

		if (vsLauncherPrefs.gameDirectory == null || vsLauncherPrefs.gameDirectory.isEmpty()) {
			loadInstallDir();
			if (installDir != null) {
				vsLauncherPrefs.gameDirectory = installDir.getPath();
			}
		}

		launch(args);

//		launchFromCmdLine(args);
	}

	static void loadPreferences() throws Exception {
		File preferences = new File("vslauncherprefs.json");

		if (!preferences.exists()) {
			if (!preferences.createNewFile()) {
				throw new IOException("Could not create preferences file!");
			}

			vsLauncherPrefs = null;
		} else {

			Gson gson = new Gson();

			Reader reader = Files.newBufferedReader(preferences.toPath());

			vsLauncherPrefs = gson.fromJson(reader, VSLauncherPrefs.class);

			reader.close();
		}
	}

	static void loadInstallDir() throws Exception {
		installDir = findInstallDirectory();

		if (installDir != null) {
			System.out.println(String.format("Game install found at %s", installDir.toString()));
		} else {
			throw new FileNotFoundException("No game install found!");
		}
	}

	/***
	 * @return A code that represents the state of the install directory
	 *
	 * 0: does not exist
	 * 1: exists
	 * 2: exists, but is not matching
	 * 3: exists, but is not a directory
	 */
	static int isValidInstallDirectory(String path) {
		File file;
		try {
			file = new File(path);
			if (!file.exists())
				return 0;
		} catch (Exception e) {
			return 0;
		}

		if (!file.isDirectory()) {
			return 3;
		}

		return file.getName().endsWith("Mods") ? 1 : 2;
	}

	private static void launchFromCmdLine(String[] args) throws Exception {
		if (args.length == 0) {
			throw new Exception("No modpack specified!");
		}

		String packLink = args[0];

		loadPreferences();
		loadInstallDir();

		updateModpackAndLaunchGame(packLink);
	}

	static void updateModpackAndLaunchGame(String packLink) throws IOException {
		Modpack pack = getModPackFromURL(packLink);

		if (vsLauncherPrefs.packId == null || !vsLauncherPrefs.packId.equalsIgnoreCase(pack.packId) || vsLauncherPrefs.modpackVersion < pack.packVersion) {

			if (vsLauncherPrefs.packId == null || !vsLauncherPrefs.packId.equalsIgnoreCase(pack.packId)) {
				System.out.println(String.format("New modpack detected!\nCurrent modpack: %s v%s\nNew modpack: %s v%s", vsLauncherPrefs.packId != null ? vsLauncherPrefs.packId : "none", vsLauncherPrefs.modpackVersion, pack.packId, pack.packVersion));
			} else {
				System.out.println(String.format("Update found!\nCurrent modpack version: %s\nLatest modpack version: %s", vsLauncherPrefs.modpackVersion, pack.packVersion));
			}

			downloadModpack(pack);

			vsLauncherPrefs.gameDirectory = installDir.getPath();
			vsLauncherPrefs.packLink = packLink;
			vsLauncherPrefs.packId = pack.packId;
			vsLauncherPrefs.modpackVersion = pack.packVersion;

			Gson gson = new Gson();
			String json = gson.toJson(vsLauncherPrefs, VSLauncherPrefs.class);

			System.out.println(String.format("out: %s", json));

			FileOutputStream fos = new FileOutputStream(new File("vslauncherprefs.json"));

			fos.write(json.getBytes(Charset.forName("UTF-8")));
			fos.close();
		}

		launchGame();
	}

	private static void downloadModpack(Modpack pack) throws IOException {
		if (installDir != null) {
			delRecursive(installDir);
		}

		if (!installDir.exists()) {
			if (!installDir.mkdir()) {
				throw new IOException("Could not regenerate '.../VintagestoryData/Mods' folder!");
			}
		}

		URL download = new URL(pack.packDownloadLink);

		ReadableByteChannel rbc = Channels.newChannel(download.openStream());
		FileOutputStream fos = new FileOutputStream("mods.zip");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		ZipFile zipFile = new ZipFile("mods.zip");

		try {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					// This is not robust, just for demonstration purposes.
					(new File(entry.getName())).mkdir();
					continue;
				}

				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(installDir.getPath() + "\\" + entry.getName())));
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}

		File file = new File("mods.zip");
		file.deleteOnExit();
	}

	private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	private static void launchGame() throws IOException {
		String str = installDir.getParentFile().getParentFile() + "\\Vintagestory\\Vintagestory.exe";

		Runtime.getRuntime().exec(str);

		stage.close();
	}

	private static boolean delRecursive(File dir) {
		return Arrays.stream(dir.listFiles()).allMatch((f) -> f.isDirectory() ? delRecursive(f) : f.delete()) && dir.delete();
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
