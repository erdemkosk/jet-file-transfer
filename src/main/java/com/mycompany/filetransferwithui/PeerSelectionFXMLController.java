package com.mycompany.filetransferwithui;

import com.jfoenix.controls.JFXCheckBox;
import com.mycompany.filetransferwithui.controllers.ApplicationSettingsManager;
import com.mycompany.filetransferwithui.controllers.PeerController;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.interfaces.IApp;
import com.mycompany.filetransferwithui.interfaces.IPeerDiscoveryObserver;
import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class PeerSelectionFXMLController implements Initializable, IPeerDiscoveryObserver, IApp {

    private Stage stage;
    private AppSettings appSettings;
    private PeerController peerController;
    private final ApplicationSettingsManager appManager = new ApplicationSettingsManager();
    private final Map<String, ServerInformation> peersById = new LinkedHashMap<>();
    private final ObservableList<ServerInformation> peerList = FXCollections.observableArrayList();
    private final Tooltip tip = new Tooltip();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedFile;

    @FXML
    private ListView<ServerInformation> peerListView;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField portText;
    @FXML
    private TextField portTextDiscovery;
    @FXML
    private TextField manualIpText;
    @FXML
    private TextField manualPortText;
    @FXML
    private Label folderLabel;
    @FXML
    private Label portLabel;
    @FXML
    private Label discoveryPortLabel;
    @FXML
    private AnchorPane h_settings;
    @FXML
    private Button selectFolderBtn;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private JFXCheckBox trayCheck;
    @FXML
    private ImageView btn_connect;
    @FXML
    private ImageView btn_setting;
    @FXML
    private ImageView btn_exit;
    @FXML
    private ImageView btn_min;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            appSettings = appManager.readDefaultConfigIfDoesNotExitCreateOne();
            seedSettings(appSettings);
            peerListView.setItems(peerList);
            peerListView.setOnMouseClicked(this::handlePeerSelected);
            statusLabel.setText("Looking for nearby devices...");
        } catch (IOException ex) {
            Logger.getLogger(PeerSelectionFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (stage == null) {
            return;
        }
        peerController = new PeerController(stage, this);
        peerController.hookMainAppObserver(this);
        peerController.start();
    }

    private void seedSettings(AppSettings settings) {
        Platform.runLater(() -> {
            portLabel.setText(Integer.toString(settings.getPortNumber()));
            discoveryPortLabel.setText(Integer.toString(settings.getDiscoveryServerPortNumber()));
            trayCheck.setSelected(settings.getShowTrayNotification());
            folderLabel.setText(settings.getSaveFolderPath());
            portText.setText(Integer.toString(settings.getPortNumber()));
            portTextDiscovery.setText(Integer.toString(settings.getDiscoveryServerPortNumber()));
            manualPortText.setText(Integer.toString(settings.getPortNumber()));
        });
    }

    @Override
    public void onPeerDiscovered(ServerInformation peer) {
        Platform.runLater(() -> {
            peersById.put(peer.getPeerId(), peer);
            peerList.setAll(peersById.values());
            if (peerList.isEmpty()) {
                statusLabel.setText("Looking for nearby devices...");
            } else {
                statusLabel.setText(peerList.size() + " device(s) found. Tap one to connect.");
            }
        });
    }

    private void handlePeerSelected(MouseEvent event) {
        if (event.getClickCount() < 1) {
            return;
        }
        ServerInformation selected = peerListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            statusLabel.setText("Connecting to " + selected.getServerHostName() + "...");
            peerController.connectToPeer(selected);
        }
    }

    @FXML
    private void handleManualConnect(MouseEvent event) {
        if (event.getSource() != btn_connect) {
            return;
        }
        if (!Helpers.NumericHelper.isNumeric(manualPortText.getText())) {
            showConnectionError();
            return;
        }
        ServerInformation manualPeer = new ServerInformation(
                "manual-" + manualIpText.getText(),
                "Manual device",
                Integer.parseInt(manualPortText.getText())
        );
        manualPeer.setServerIP(manualIpText.getText().trim());
        statusLabel.setText("Connecting manually...");
        peerController.connectToPeer(manualPeer);
    }

    public void showConnectionError() {
        Platform.runLater(() -> {
            statusLabel.setText("Connection failed. Try again.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not connect to the selected device.", ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();
        });
    }

    @FXML
    private void handleButtonHoverEnter(MouseEvent event) {
        tip.setStyle("-fx-font: normal bold 12 Langdon; -fx-base: #AE3522; -fx-text-fill: orange;");
        if (event.getSource() == btn_connect) {
            btn_connect.setOpacity(0.7);
            tip.setText("Connect manually");
            Tooltip.install(btn_connect, tip);
        } else if (event.getSource() == btn_setting) {
            btn_setting.setOpacity(0.7);
            tip.setText("Settings");
            Tooltip.install(btn_setting, tip);
        } else if (event.getSource() == btn_exit) {
            btn_exit.setOpacity(0.7);
            tip.setText("Exit");
            Tooltip.install(btn_exit, tip);
        } else if (event.getSource() == btn_min) {
            btn_min.setOpacity(0.7);
            tip.setText("Minimize");
            Tooltip.install(btn_min, tip);
        }
    }

    @FXML
    private void handleButtonHoverExit(MouseEvent event) {
        if (event.getSource() == btn_connect) {
            btn_connect.setOpacity(0.5);
            Tooltip.uninstall(btn_connect, tip);
        } else if (event.getSource() == btn_setting) {
            btn_setting.setOpacity(0.5);
            Tooltip.uninstall(btn_setting, tip);
        } else if (event.getSource() == btn_exit) {
            btn_exit.setOpacity(0.5);
            Tooltip.uninstall(btn_exit, tip);
        } else if (event.getSource() == btn_min) {
            btn_min.setOpacity(0.5);
            Tooltip.uninstall(btn_min, tip);
        }
    }

    @FXML
    private void handleButtonClicks(MouseEvent event) throws IOException {
        if (event.getSource() == btn_exit) {
            if (peerController != null) {
                peerController.ExitRequested();
            } else {
                applicationExit();
            }
        } else if (event.getSource() == selectFolderBtn) {
            directoryChooser.setTitle("Select Saved Folder");
            selectedFile = directoryChooser.showDialog(stage);
            if (selectedFile != null) {
                String decodedPath = URLDecoder.decode(selectedFile.getAbsolutePath(), "UTF-8");
                selectedFile = new File(decodedPath);
                folderLabel.setText(decodedPath);
            }
        } else if (event.getSource() == btnSave) {
            if (!Helpers.NumericHelper.isNumeric(portText.getText()) || !Helpers.NumericHelper.isNumeric(portTextDiscovery.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Application settings cannot be changed. Please check your settings.", ButtonType.OK);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.show();
                return;
            }
            portLabel.setText(portText.getText());
            discoveryPortLabel.setText(portTextDiscovery.getText());
            if (folderLabel.getText() != null) {
                appSettings.setSaveFolderPath(folderLabel.getText());
            }
            appSettings.setPortNumber(Integer.parseInt(portLabel.getText()));
            appSettings.setDiscoveryServerPortNumber(Integer.parseInt(discoveryPortLabel.getText()));
            appSettings.setShowTrayNotification(trayCheck.isSelected());
            appManager.writeConfigSettingsDeleteOldOne(appSettings);
            manualPortText.setText(portLabel.getText());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Application settings changed successfully!", ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            h_settings.setVisible(false);
            btn_setting.setOpacity(0.5);
        } else if (event.getSource() == btnCancel) {
            h_settings.setVisible(false);
            btn_setting.setOpacity(0.5);
        } else if (event.getSource() == btn_setting) {
            h_settings.setVisible(true);
            btn_setting.setOpacity(0.7);
        } else if (event.getSource() == btn_min) {
            stage.setIconified(true);
        }
    }

    private void applicationExit() {
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void exitRequested() {
        applicationExit();
    }
}
