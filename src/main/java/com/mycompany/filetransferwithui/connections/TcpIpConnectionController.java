/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.mycompany.filetransferwithui.MainFXMLController;
import com.mycompany.filetransferwithui.controllers.ApplicationSettingsManager;
import com.mycompany.filetransferwithui.controllers.FileServerController;
import com.mycompany.filetransferwithui.enums.TcpIpType;
import com.mycompany.filetransferwithui.models.AppSettings;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author mek
 */
public class TcpIpConnectionController {

    protected ApplicationSettingsManager appManager;
    protected AppSettings appSetting;
    protected MainFXMLController controller;
    protected double xOffset = 0;
    protected double yOffset = 0;
    protected Stage stage;
    protected AtomicInteger processedThreadCount = new AtomicInteger();

    protected void readAndLoadSettings() {

        appManager = new ApplicationSettingsManager();
        try {
            appSetting = appManager.readDefaultConfigIfDoesNotExitCreateOne();
        } catch (IOException ex) {
            Logger.getLogger(FileServerController.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    protected void showMainWindow(TcpIpType type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainFXML.fxml")
            );

            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root, 425, 466);

            controller = loader.getController();
            controller = loader.<MainFXMLController>getController();
            controller.setAppSettings(appSetting);
            controller.seedItemsForConnectionType(type);

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });

            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            stage.show();

        } catch (IOException ex) {

        }

        //scene.getStylesheets().add("/styles/Styles.css");
    }

    public AtomicInteger getProcessedThreadCount() {
        return processedThreadCount;
    }

    public void setProcessedThreadCount(AtomicInteger processedThreadCount) {
        this.processedThreadCount = processedThreadCount;
    }

}
