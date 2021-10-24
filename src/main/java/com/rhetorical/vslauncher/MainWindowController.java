package com.rhetorical.vslauncher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

	@FXML
	public TextField jsonAddress;

	@FXML
	public TextField gameDirectory;

	@FXML
	public Button launchButton;

	@FXML
	public Label errorLabel;

	public void launchButtonPress() {

		VSLauncherPrefs prefs = VSLauncher.vsLauncherPrefs;

		int code = VSLauncher.isValidInstallDirectory(gameDirectory.getText());

		if (code == 0) {
			errorLabel.setText("That directory does not exist!");
			return;
		} else if (code == 2) {
			errorLabel.setText("Invalid! Must end with \"Mods\"");
			return;
		} else if (code == 3) {
			errorLabel.setText("Invalid game directory! Must be a directory!");
			return;
		} else if (code == 1) {
			prefs.gameDirectory = gameDirectory.getText();
			VSLauncher.installDir = new File(gameDirectory.getText());
		}

		String packLink = jsonAddress.getText();

		try {
			VSLauncher.updateModpackAndLaunchGame(packLink);
		} catch (Exception e) {
			errorLabel.setText("Could not download pack!");
			e.printStackTrace();
		}
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		VSLauncherPrefs prefs = VSLauncher.vsLauncherPrefs;

		jsonAddress.setText(prefs.packLink);

		gameDirectory.setText(prefs.gameDirectory);

	}
}
