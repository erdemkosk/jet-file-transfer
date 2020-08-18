package com.mycompany.filetransferwithui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mycompany.filetransferwithui.models.AppSettings;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Mustafa Erdem Köşk<erdemkosk@gmail.com>
 */
public class NotificationFXMLController implements Initializable {

	/**
	 * Initializes the controller class.
	 */
	@FXML
	private Label totalFileWaited, status, totalFile;
	@FXML
	private ImageView btn_folder;
	private AppSettings appSettings;
	Tooltip tip = new Tooltip();
	int totalfileCount = 0;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO
	}

	public void setWaitedFileText(int count) {
		Platform.runLater(
				() -> {
					totalFileWaited.setText(count + "");

					if (count > 0) {
						totalfileCount++;
						totalFile.setText(totalfileCount + "");

					}
				}
		);
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}

	public void setAppSettings(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	@FXML
	private void handleButtonAction(MouseEvent event) throws IOException {
		Desktop.getDesktop().open(new File(appSettings.getSaveFolderPath()));
	}

	@FXML
	private void handleButtonHoverEnter(MouseEvent event) {
		btn_folder.setOpacity(0.7);
		tip.setText("Open Download Directory");
		Tooltip.install(btn_folder, tip);
	}

	@FXML
	private void handleButtonHoverExit(MouseEvent event) {
		btn_folder.setOpacity(0.5);
	}

	public void setStatusText(int count) {

		Platform.runLater(
				() -> {
					if (count > 0) {
						status.setText("Process!");
						status.setTextFill(Color.web("#e67e22"));
					} else {
						status.setText("Sending..");
						status.setTextFill(Color.WHITE);

					}
				}
		);
	}

	public void setStatusText(boolean isReady) {
		Platform.runLater(
				() -> {
					if (status.getText().equals("Process!") != true) {
						if (isReady == true) {
							status.setText("Ready!");

						} else {

							status.setText("Sending..");

						}
					}

				}
		);
	}
}
