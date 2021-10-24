package com.rhetorical.vslauncher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
