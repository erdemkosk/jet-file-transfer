/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import com.mycompany.filetransferwithui.enums.TcpIpType;
import com.mycompany.filetransferwithui.interfaces.IMainUIObserver;
import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.FileItemModel;
import com.mycompany.filetransferwithui.models.FileListViewCell;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import tray.notification.TrayNotification;

import tray.animations.AnimationType;

/**
 *
 * @author mek
 */
public class MainFXMLController implements Initializable {

    private AppSettings appSettings;
    private ArrayList<IMainUIObserver> observers = new ArrayList<IMainUIObserver>();
    private ObservableList<ServerInformation> serverObservableList = FXCollections.observableArrayList();
    private List<File> selectedFiles;
    @FXML
    private ImageView btn_user, btn_exit, btn_min, btn_download, btn_upload, btn_app, btn_app_s;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button btnAutoLogin, btnManuelLogin;
    @FXML
    private JFXHamburger hamburger;
    @FXML
    private TextField textIP, textPort;
    @FXML
    private JFXSnackbar snackBar, connectionSnackbar;

    @FXML
    private JFXDrawer drawer;
    @FXML
    private JFXComboBox<ServerInformation> serverCombo;
    @FXML
    private AnchorPane h_settings, h_user, h_download, h_login, h_upload, h_connectwait;
    @FXML
    private ListView<FileItemModel> listViewComing;
    private ObservableList<FileItemModel> fileObservableComeList;
    @FXML
    private ListView<FileItemModel> listViewSending;
    private ObservableList<FileItemModel> fileObservableSendList;
    Tooltip tip = new Tooltip();
    private ImageView btn_selected;
    private boolean isReadyForAnimation = true;
    private NotificationFXMLController notificationController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void seedItemsForConnectionType(TcpIpType type) {
        switch (type) {
            case FileServer:
                h_login.setVisible(false);
                h_connectwait.setVisible(true);
                //progress.startFullDrag();
                progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                // btn_settings.setDisable(true);
                btn_download.setDisable(true);
                btn_upload.setDisable(true);
                btn_user.setDisable(true);
                readForShowIpAndPassword();
                break;
            case FileClient:
                h_login.setVisible(true);

                break;

        }
        //seed();
        seedVariables(appSettings);
    }

    private void seedVariables(final AppSettings appSettings) {
        Platform.runLater(
                () -> {
                    serverCombo.setItems(serverObservableList);
                    tip.setStyle("-fx-font: normal bold 12 Langdon; "
                            + "-fx-base: #AE3522; "
                            + "-fx-text-fill: orange;");
                    fileObservableComeList = FXCollections.observableArrayList();

                    listViewComing.setItems(fileObservableComeList);
                    listViewComing.setCellFactory(fileListComing -> new FileListViewCell());

                    fileObservableSendList = FXCollections.observableArrayList();

                    listViewSending.setItems(fileObservableSendList);
                    listViewSending.setCellFactory(fileListSend -> new FileListViewCell());
                    //listView.setStyle(".table-row-cell:filled:selected,.table-row-cell:filled > .table-cell:selected {-fx-background: red;}");
                    h_upload.setOnDragOver(new EventHandler<DragEvent>() {
                        @Override
                        public void handle(DragEvent event) {
                            Dragboard db = event.getDragboard();

                            if (db.hasFiles()) {
                                event.acceptTransferModes(TransferMode.COPY);
                            } else {
                                event.consume();
                            }

                        }
                    });
                    h_upload.setOnDragDropped(new EventHandler<DragEvent>() {
                        @Override
                        public void handle(DragEvent event) {
                            Dragboard db = event.getDragboard();
                            List<File> files = event.getDragboard().getFiles();
                            boolean success = false;
                            if (db.hasFiles()) {

                                success = true;

                                selectedFiles = files;
                                notifyObserverForSelectedFiles(selectedFiles);
                            }
                            event.setDropCompleted(success);
                            event.consume();
                        }
                    });

                    listViewComing.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {

                            event.consume();
                        }
                    });
                    listViewSending.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent event) {

                            event.consume();
                        }
                    });
                    snackBar = new JFXSnackbar(h_upload);

                    Scene scene = (Scene) h_upload.getScene();
                    String css = this.getClass().getResource("/styles/snack.css").toExternalForm();
                    scene.getStylesheets().add(css);
                    try {

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotificationFXML.fxml"));
                        AnchorPane pane = loader.load();

                        drawer.setSidePane(pane);
                        notificationController = (NotificationFXMLController) loader.getController();
                        notificationController.setAppSettings(appSettings);
                    } catch (IOException ex) {
                        Logger.getLogger(MainFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    HamburgerSlideCloseTransition transition = new HamburgerSlideCloseTransition(hamburger);
                    transition.setRate(-1);
                    hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                        transition.setRate(transition.getRate() * -1);
                        transition.play();
                        if (drawer.isShown() == true) {
                            drawer.close();
                        } else {
                            drawer.open();
                        }
                    });
                    transition.setRate(transition.getRate() * -1);
                    transition.play();
                    if (drawer.isShown() == true) {
                        drawer.close();
                    } else {
                        drawer.open();
                    }

                }
        );

    }

    @FXML
    private void handleInsideButtons(MouseEvent event) {

        if (event.getSource() == btnAutoLogin) {
            ServerInformation selected = serverCombo.getSelectionModel().getSelectedItem();
            selected.setServerIP(selected.getServerIP().substring(1));

            if (selected != null) {
                notifyConnectServer(selected);

            } else {
                //alert
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have to select at least one item... ", ButtonType.OK);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.show();
            }
        } else if (event.getSource() == btnManuelLogin) {
            ServerInformation selected = new ServerInformation(textIP.getText(), Integer.parseInt(textPort.getText()));
            notifyConnectServer(selected);

        }
    }

    @FXML
    private void handleButtonAction(MouseEvent event) {
        btn_selected = (ImageView) event.getTarget();
        if (event.getTarget() == btn_user) {
            h_user.setVisible(true);
            h_settings.setVisible(false);
            h_download.setVisible(false);
            h_upload.setVisible(false);
            btn_user.setOpacity(0.7);

            btn_exit.setOpacity(0.5);
            btn_min.setOpacity(0.5);
            btn_download.setOpacity(0.5);
            btn_upload.setOpacity(0.5);
        } else if (event.getTarget() == btn_exit) {
            h_user.setVisible(false);
            h_settings.setVisible(false);
            h_download.setVisible(false);
            h_upload.setVisible(false);
            btn_exit.setOpacity(0.7);
            btn_user.setOpacity(0.5);
            btn_min.setOpacity(0.5);
            btn_download.setOpacity(0.5);
            btn_upload.setOpacity(0.5);
            notifyObserverForExit();

        } else if (event.getTarget() == btn_min) {
            Stage stage = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
            stage.setIconified(true);

        } else if (event.getTarget() == btn_download) {
            h_user.setVisible(false);
            h_settings.setVisible(false);
            h_download.setVisible(true);
            h_upload.setVisible(false);
            btn_download.setOpacity(0.7);
            btn_user.setOpacity(0.5);
            btn_exit.setOpacity(0.5);
            btn_min.setOpacity(0.5);

            btn_upload.setOpacity(0.5);
        } else if (event.getTarget() == btn_upload) {
            h_user.setVisible(false);
            h_settings.setVisible(false);
            h_download.setVisible(false);
            h_upload.setVisible(true);
            btn_upload.setOpacity(0.7);
            btn_user.setOpacity(0.5);
            btn_exit.setOpacity(0.5);
            btn_min.setOpacity(0.5);
            btn_download.setOpacity(0.5);
            snackBar.show("      Drag and drop file(s) or folder(s)      ", 3000);
        }

    }

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void hookClientObserver(IMainUIObserver client) {
        observers.add(client);
    }

    public void unHookClientObserver(IMainUIObserver client) {
        observers.remove(client);
    }

    private void notifyConnectServer(ServerInformation inform) {

        observers.forEach((temp) -> {
            temp.connectServerRequested(inform);
        });
    }

    public void addServerToComboBox(ServerInformation information) {

        Platform.runLater(
                () -> {
                    serverObservableList.add(0, information);
                    serverCombo.getSelectionModel().select(information);
                }
        );

        // serverCombo.getSelectionModel().select(0);
    }

    public void serverConnectedSuccess() {
        h_login.setVisible(false);
    }

    public void serverConnectedFailed() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Connection Error ", ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }

    private void notifyObserverForSelectedFiles(List<File> selectedFiles) {
        if (selectedFiles.size() > 0) {
            runAnimation();
        }

        for (IMainUIObserver temp : observers) {
            temp.newFilesTakenRequested(selectedFiles);
        }
    }

    private void notifyObserverForExit() {
        for (IMainUIObserver temp : observers) {
            temp.ExitRequested();
        }
    }

    public void postNewFileRequested(FileItemModel model) {

        Platform.runLater(
                () -> {
                    fileObservableSendList.add(0, model);
                }
        );

    }

    public void postPertangeChangeRequested(FileItemModel model, double value) {

        Platform.runLater(
                () -> {
                    int index = fileObservableSendList.indexOf(model);
                    fileObservableSendList.get(index).setPercentageOfLoadedFile(value);

                    listViewSending.refresh();
                }
        );

    }

    public void getNewFileRequested(FileItemModel model) {
        runAnimation();
        Platform.runLater(
                () -> {

                    fileObservableComeList.add(0, model);
                    //(model.getFileName() + " receiving file!");
                }
        );

    }

    public void getPertangeChangeRequested(FileItemModel model, double value) {
        Platform.runLater(
                () -> {
                    int index = fileObservableComeList.indexOf(model);
                    fileObservableComeList.get(index).setPercentageOfLoadedFile(value);

                    listViewComing.refresh();
                }
        );

    }

    @FXML
    private void handleButtonHoverEnter(MouseEvent event) {

        if (event.getSource() == btn_user) {
            btn_user.setOpacity(0.7);
            tip.setText("About");
            Tooltip.install(btn_user, tip);

        } else if (event.getSource() == btn_min) {

            btn_min.setOpacity(0.7);
            tip.setText("Minimanize");
            Tooltip.install(btn_min, tip);

        } else if (event.getSource() == btn_upload) {

            btn_upload.setOpacity(0.7);
            tip.setText("Send Files");
            Tooltip.install(btn_upload, tip);

        } else if (event.getSource() == btn_download) {

            btn_download.setOpacity(0.7);
            tip.setText("Get Files");
            Tooltip.install(btn_download, tip);

        } else if (event.getSource() == btn_exit) {

            btn_exit.setOpacity(0.7);
            tip.setText("Exit");
            Tooltip.install(btn_exit, tip);
        } else if (event.getSource() == btn_app) {
            btn_app.setImage(new Image(getClass().getResource("/images/app.png").toString(), true));
            btn_app.setOpacity(0.7);

        } else if (event.getSource() == btn_app_s) {
            btn_app_s.setImage(new Image(getClass().getResource("/images/app.png").toString(), true));
            btn_app_s.setOpacity(0.7);
        }

    }

    @FXML
    private void handleButtonHoverExit(MouseEvent event) {

        if (event.getSource() == btn_user && btn_selected != btn_user) {
            btn_user.setOpacity(0.5);
            Tooltip.uninstall(btn_user, tip);

        } else if (event.getSource() == btn_min && btn_selected != btn_min) {

            btn_min.setOpacity(0.5);
            Tooltip.uninstall(btn_min, tip);

        } else if (event.getSource() == btn_upload && btn_selected != btn_upload) {

            btn_upload.setOpacity(0.5);
            Tooltip.uninstall(btn_upload, tip);

        } else if (event.getSource() == btn_download && btn_selected != btn_download) {

            btn_download.setOpacity(0.5);
            Tooltip.uninstall(btn_download, tip);

        } else if (event.getSource() == btn_exit && btn_selected != btn_exit) {

            btn_exit.setOpacity(0.5);
            Tooltip.uninstall(btn_exit, tip);

        } else if (event.getSource() == btn_app) {

            btn_app.setOpacity(0.5);
            btn_app.setImage(new Image(getClass().getResource("/images/app-t.png").toString(), true));

        } else if (event.getSource() == btn_app_s) {
            btn_app_s.setOpacity(0.5);
            btn_app_s.setImage(new Image(getClass().getResource("/images/app-t.png").toString(), true));
        }

    }

    public void clientConnected() {

        h_connectwait.setVisible(false);
        btn_download.setDisable(false);
        btn_upload.setDisable(false);
        btn_user.setDisable(false);
    }

    public void showNotifications(String message) {
        Platform.runLater(
                () -> {
                    if (appSettings.getShowTrayNotification() == true) {
                        try {

                            Image programIcon = new Image(getClass().getResource("/images/app.png").toString(), true);

                            TrayNotification tray = new TrayNotification();
                            tray.setTitle("Jet FileTransfer");
                            tray.setMessage(message);
                            tray.setRectangleFill(Paint.valueOf("#e67e22"));
                            tray.setAnimationType(AnimationType.SLIDE);
                            tray.setImage(programIcon);
                            tray.showAndDismiss(Duration.seconds(5));
                        } catch (Exception ex) {
                            System.out.println("hata");
                        }
                    }

                });
    }

    public void runAnimation() {
        Platform.runLater(
                () -> {
                    if (isReadyForAnimation == true) {
                        isReadyForAnimation = false;
                        RotateTransition rt = new RotateTransition(Duration.millis(1000), btn_app_s);
                        rt.setByAngle(360);
                        rt.setAutoReverse(true);
                        rt.playFromStart();
                        btn_app_s.setImage(new Image(getClass().getResource("/images/app.png").toString(), true));
                        btn_app_s.setOpacity(0.7);
                        rt.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                btn_app_s.setOpacity(0.5);
                                btn_app_s.setImage(new Image(getClass().getResource("/images/app-t.png").toString(), true));
                                isReadyForAnimation = true;
                            }

                        });
                    }
                }
        );
    }

    public void notifyUserForProgressedFiles(int count) {
        notificationController.setWaitedFileText(count);
    }

    private void readForShowIpAndPassword() {
        connectionSnackbar = new JFXSnackbar(h_connectwait);
        try {
            connectionSnackbar.show("Server " + InetAddress.getLocalHost().getHostAddress().toString() + ":" + appSettings.getPortNumber(), 6000);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showThreadStatus(int count) {

        notificationController.setStatusText(count);
    }

    public void setStatusText(boolean isReady) {

        notificationController.setStatusText(isReady);
    }
}
