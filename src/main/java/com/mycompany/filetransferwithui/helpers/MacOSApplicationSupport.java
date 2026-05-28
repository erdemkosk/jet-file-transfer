package com.mycompany.filetransferwithui.helpers;

import com.mycompany.filetransferwithui.MainApp;
import com.mycompany.filetransferwithui.controllers.OsCheck;
import java.awt.Desktop;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.ImageIcon;

public final class MacOSApplicationSupport {

    private static final Logger LOGGER = Logger.getLogger(MacOSApplicationSupport.class.getName());
    private static final String DOCK_ICON_RESOURCE = "/images/app-mac-dock.png";

    private MacOSApplicationSupport() {
    }

    public static void initialize(Runnable onQuitRequested) {
        if (Helpers.OperatingSystemHelper.detectOperatingSystem() != OsCheck.OSType.MacOS) {
            return;
        }
        installQuitHandler(onQuitRequested);
        Platform.runLater(MacOSApplicationSupport::applyDockIcon);
    }

    public static void applyDockIcon() {
        if (Helpers.OperatingSystemHelper.detectOperatingSystem() != OsCheck.OSType.MacOS) {
            return;
        }

        URL iconUrl = MainApp.class.getResource(DOCK_ICON_RESOURCE);
        if (iconUrl == null) {
            iconUrl = MainApp.class.getResource("/images/app.png");
        }
        if (iconUrl == null) {
            return;
        }

        try {
            java.awt.Image dockImage = new ImageIcon(iconUrl).getImage();
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            Object application = applicationClass.getMethod("getApplication").invoke(null);
            applicationClass.getMethod("setDockIconImage", java.awt.Image.class)
                    .invoke(application, dockImage);
        } catch (ReflectiveOperationException ex) {
            LOGGER.log(Level.WARNING, "Could not set macOS dock icon", ex);
        }
    }

    private static void installQuitHandler(Runnable onQuitRequested) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            return;
        }

        desktop.setQuitHandler((event, response) -> Platform.runLater(() -> {
            try {
                onQuitRequested.run();
            } finally {
                response.performQuit();
            }
        }));
    }
}
