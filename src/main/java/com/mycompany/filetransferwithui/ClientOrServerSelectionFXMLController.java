/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui;

import com.jfoenix.controls.JFXCheckBox;
import com.mycompany.filetransferwithui.controllers.ApplicationSettingsManager;
import com.mycompany.filetransferwithui.controllers.FileClientController;
import com.mycompany.filetransferwithui.controllers.FileServerController;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.interfaces.IApp;
import com.mycompany.filetransferwithui.interfaces.IController;
import com.mycompany.filetransferwithui.models.AppSettings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author mek
 */
public class ClientOrServerSelectionFXMLController implements Initializable, IApp {

    private Stage stage;
    Tooltip tip = new Tooltip();

    IController fileController;
    private AppSettings appSettings;
    @FXML
    private TextField portText, portTextDiscovery;
    @FXML
    private Label folderLabel, portLabel, discoveryPortLabel;
    @FXML
    private AnchorPane h_settings;
    @FXML
    private Button selectFolderBtn, btnSave, btnCancel;

    @FXML
    private JFXCheckBox trayCheck;
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedFile;
    private ApplicationSettingsManager appManager = new ApplicationSettingsManager();
    private ImageView btn_selected;

    @FXML
    private ImageView btn_server, btn_client, btn_exit, btn_setting, btn_min;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            appSettings = appManager.readDefaultConfigIfDoesNotExitCreateOne();
            seedItems(appSettings);
        } catch (IOException ex) {
            Logger.getLogger(ClientOrServerSelectionFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void seedItems(AppSettings appSettings) {
        Platform.runLater(
                () -> {

                    btnSave.setFocusTraversable(false);

                    portLabel.setText(Integer.toString(appSettings.getPortNumber()));
                    discoveryPortLabel.setText(Integer.toString(appSettings.getDiscoveryServerPortNumber()));
                    trayCheck.setSelected(appSettings.getShowTrayNotification());

                    folderLabel.setText(appSettings.getSaveFolderPath());

                    portText.setText(Integer.toString(appSettings.getPortNumber()));
                    portTextDiscovery.setText(Integer.toString(appSettings.getDiscoveryServerPortNumber()));

                    //ıpLabel.setText(getLocalAddress().getHostAddress());
                });

    }

    @FXML

    private void handleButtonHoverEnter(MouseEvent event) {

        tip.setStyle("-fx-font: normal bold 12 Langdon; "
                + "-fx-base: #AE3522; "
                + "-fx-text-fill: orange;");
        if (event.getSource() == btn_server) {

            //btn_server.setImage(new Image(getClass().getResource("/images/server-hover.png").toString(), true));
            btn_server.setOpacity(0.7);
            tip.setText("Server");
            Tooltip.install(btn_server, tip);

        } else if (event.getSource() == btn_client) {
            btn_client.setOpacity(0.7);
            tip.setText("Client");
            Tooltip.install(btn_client, tip);

        } else if (event.getSource() == btn_exit) {
            tip.setText("Exit");
            Tooltip.install(btn_exit, tip);

            btn_exit.setOpacity(0.7);

        } else if (event.getSource() == btn_setting) {
            tip.setText("Settings");
            Tooltip.install(btn_setting, tip);

            btn_setting.setOpacity(0.7);

        } else if (event.getSource() == btn_min) {

            Tooltip.install(btn_min, tip);

            btn_min.setOpacity(0.7);

        }

    }

    @FXML
    private void handleButtonHoverExit(MouseEvent event) {

        if (event.getSource() == btn_server) {

            // btn_server.setImage(new Image(getClass().getResource("/images/server.png").toString(), true));
            btn_server.setOpacity(0.5);
            Tooltip.uninstall(btn_server, tip);
        } else if (event.getSource() == btn_client) {
            btn_client.setOpacity(0.5);
            Tooltip.uninstall(btn_client, tip);
        } else if (event.getSource() == btn_exit) {
            btn_exit.setOpacity(0.5);
            Tooltip.uninstall(btn_exit, tip);
        } else if (event.getSource() == btn_setting && btn_selected != btn_setting) {
            btn_setting.setOpacity(0.5);
            Tooltip.uninstall(btn_setting, tip);
        } else if (event.getSource() == btn_min) {
            btn_min.setOpacity(0.5);
            Tooltip.uninstall(btn_min, tip);
        }

    }

    @FXML
    private void handleButtonClicks(MouseEvent event) throws IOException {
        if (event.getTarget().getClass() == ImageView.class) {
            btn_selected = (ImageView) event.getTarget();
        }
        if (event.getSource() == btn_server) {
            fileController = new FileServerController(stage);
            fileController.hookMainAppObserver(this);
            fileController.run();

        } else if (event.getSource() == btn_client) {
            fileController = new FileClientController(stage);
            fileController.hookMainAppObserver(this);
            fileController.run();

        } else if (event.getSource() == btn_exit) {
            applicationExit();
        } else if (event.getSource() == selectFolderBtn) {

            directoryChooser.setTitle("Select Saved Folder");
            selectedFile = directoryChooser.showDialog(stage);
            if (selectedFile != null) {
                String decodedPath = URLDecoder.decode(selectedFile.getAbsolutePath(), "UTF-8");
                selectedFile = new File(decodedPath);
                folderLabel.setText(decodedPath);
            }

        } else if (event.getSource() == btnSave) {

            if (Helpers.NumericHelper.isNumeric(portText.getText()) == false || Helpers.NumericHelper.isNumeric(portTextDiscovery.getText()) == false) {
                //hata
                Alert alert = new Alert(Alert.AlertType.ERROR, "Application settings cannot changed! "
                        + "Please check your settings... ", ButtonType.OK);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.show();

            } else {

                portLabel.setText(portText.getText());
                discoveryPortLabel.setText(portTextDiscovery.getText());
                if (folderLabel.getText() != null) {
                    appSettings.setSaveFolderPath(folderLabel.getText());
                }
                if (portLabel.getText() != null) {
                    appSettings.setPortNumber(Integer.parseInt(portLabel.getText()));
                }
                if (discoveryPortLabel.getText() != null) {
                    appSettings.setDiscoveryServerPortNumber(Integer.parseInt(discoveryPortLabel.getText()));
                }
                appSettings.setShowTrayNotification(trayCheck.isSelected());
                appManager.writeConfigSettingsDeleteOldOne(appSettings);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Application settings changed successfully! ", ButtonType.OK);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.showAndWait();
                h_settings.setVisible(false);
                btn_setting.setOpacity(0.5);

            }

        } else if (event.getSource() == btnCancel) {

            h_settings.setVisible(false);

        } else if (event.getSource() == btn_setting) {

            h_settings.setVisible(true);

        } else if (event.getSource() == btn_min) {

            Stage stage = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
            stage.setIconified(true);

        }

    }

    private void applicationExit() {

        Platform.exit();
        System.exit(0);
    }

    @Override
    public void exitRequested() { // son çıkış noktası

        if (fileController != null) {
            fileController.unHookMainAppObserver(this);
            fileController.disconnect();
        }

        applicationExit();
    }

    public void setStage(Stage stage) {
        this.stage = stage;

    }

}
