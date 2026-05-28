package com.mycompany.filetransferwithui;

import com.mycompany.filetransferwithui.controllers.OsCheck;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.helpers.MacOSApplicationSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.mycompany.filetransferwithui.interfaces.IControllerTemplate;

public class MainApp extends Application implements IControllerTemplate {

    private PeerSelectionFXMLController controller;
    private double xOffset = 0;
    private double yOffset = 0;

    private Stage stage;

    @Override
    public void start(final Stage stage) throws Exception {
        this.stage = stage;
        beforeStart();
        doStart();
        afterStart();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void checkAndManageAppIcon() {
        OsCheck.OSType ostype = Helpers.OperatingSystemHelper.detectOperatingSystem();
        switch (ostype) {
            case Windows:
                if (stage != null) {
                    stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/app.png")));
                }
                break;
            case MacOS:
                MacOSApplicationSupport.applyDockIcon();
                break;
            case Linux:
                break;
            case Other:
                break;
        }
    }

    @Override
    public void beforeStart() {
        checkAndManageAppIcon();
    }

    @Override
    public void doStart() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/PeerSelectionFXML.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Scene scene = new Scene(root, 400, 500);
            //scene.getStylesheets().add("/styles/Styles.css");
            
            controller = fxmlLoader.getController();
            
            stage.initStyle(StageStyle.TRANSPARENT);
            
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
            
            controller.setStage(stage);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void afterStart() {
        stage.show();
        MacOSApplicationSupport.initialize(this::shutdownApplication);
    }

    private void shutdownApplication() {
        if (controller != null) {
            controller.requestApplicationExit();
        } else {
            Platform.exit();
            System.exit(0);
        }
    }

}
