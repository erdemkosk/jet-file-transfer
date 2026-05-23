package com.mycompany.filetransferwithui.controllers;

import com.mycompany.filetransferwithui.connections.FileClient;
import com.mycompany.filetransferwithui.connections.FileServer;
import com.mycompany.filetransferwithui.connections.PeerDiscoveryService;
import com.mycompany.filetransferwithui.connections.TcpIpConnectionController;
import com.mycompany.filetransferwithui.enums.TcpIpType;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.interfaces.IApp;
import com.mycompany.filetransferwithui.interfaces.IController;
import com.mycompany.filetransferwithui.interfaces.IMainUIObserver;
import com.mycompany.filetransferwithui.interfaces.IPeerDiscoveryObserver;
import com.mycompany.filetransferwithui.interfaces.ITcpIpObserver;
import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.FileItemModel;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

public class PeerController extends TcpIpConnectionController
        implements IController, IPeerDiscoveryObserver, IMainUIObserver, ITcpIpObserver {

    private final IPeerDiscoveryObserver peerListObserver;
    private FileServer fileServer;
    private FileClient fileClient;
    private PeerDiscoveryService peerDiscovery;
    private final ArrayList<IApp> mainAppObservers = new ArrayList<>();
    private final BlockingQueue<FileItemModel> queue = new LinkedBlockingDeque<>();
    private volatile boolean connected;

    public PeerController(Stage stage, IPeerDiscoveryObserver peerListObserver) {
        this.stage = stage;
        this.peerListObserver = peerListObserver;
    }

    public void start() {
        readAndLoadSettings();
        applySaveFolder(appSetting.getSaveFolderPath());
        fileServer = new FileServer(appSetting, queue);
        fileServer.hookObservers(this);
        fileServer.start();

        peerDiscovery = new PeerDiscoveryService(appSetting);
        peerDiscovery.addObserver(this);
        peerDiscovery.addObserver(peerListObserver);
        peerDiscovery.start();
    }

    public void applySettings(AppSettings settings) {
        this.appSetting = settings;
        applySaveFolder(settings.getSaveFolderPath());
    }

    private void applySaveFolder(String saveFolderPath) {
        if (saveFolderPath == null || saveFolderPath.trim().isEmpty()) {
            return;
        }
        String normalizedPath = new File(saveFolderPath.trim()).getAbsolutePath();
        appSetting.setSaveFolderPath(normalizedPath);
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(normalizedPath));
        } catch (IOException ex) {
            Logger.getLogger(PeerController.class.getName()).log(Level.WARNING, "Could not create save folder", ex);
        }
        if (fileServer != null) {
            fileServer.setSaveFolderPath(normalizedPath);
        }
        if (fileClient != null) {
            fileClient.setSaveFolderPath(normalizedPath);
        }
    }

    private void reloadSettingsFromDisk() {
        readAndLoadSettings();
        applySaveFolder(appSetting.getSaveFolderPath());
    }

    public void connectToPeer(ServerInformation peer) {
        if (connected || peer == null) {
            return;
        }

        reloadSettingsFromDisk();
        fileServer.setAcceptingConnections(false);
        fileClient = new FileClient(appSetting, queue);
        fileClient.hookObservers(this);

        try {
            ServerInformation target = peer;
            if (target.getServerIP() != null) {
                target.setServerIP(com.mycompany.filetransferwithui.helpers.NetworkHelper.normalizeIp(target.getServerIP()));
            }
            fileClient.connectToServer(target);
            connected = true;
            peerDiscovery.stop();
            showMainWindow(TcpIpType.FileClient);
            controller.hookClientObserver(this);
            controller.serverConnectedSuccess();
        } catch (Exception ex) {
            fileServer.setAcceptingConnections(true);
            Logger.getLogger(PeerController.class.getName()).log(Level.SEVERE, null, ex);
            if (peerListObserver instanceof com.mycompany.filetransferwithui.PeerSelectionFXMLController) {
                ((com.mycompany.filetransferwithui.PeerSelectionFXMLController) peerListObserver).showConnectionError();
            }
        }
    }

    @Override
    public void onPeerDiscovered(ServerInformation peer) {
        // Forwarded through peerDiscovery observer list to UI.
    }

    @Override
    public void connectServerRequested(ServerInformation inform) {
    }

    @Override
    public void newFilesTakenRequested(List<File> selectedFiles) {
        new Thread(() -> {
            try {
                ArrayList<FileItemModel> models = getActiveTransfer().generateItemModels(selectedFiles);
                for (FileItemModel file : models) {
                    getActiveTransfer().getFileQueue().put(file);
                }
                fileProgressedRequested(getActiveTransfer().getFileQueue().size());
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(PeerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    private com.mycompany.filetransferwithui.connections.TcpConnections getActiveTransfer() {
        return fileClient != null && fileClient.isIsConnected() ? fileClient : fileServer;
    }

    @Override
    public void newPostFileRequested(FileItemModel model) {
        controller.postNewFileRequested(model);
    }

    @Override
    public void percentageChangedPostRequested(FileItemModel file, double percentage) {
        controller.postPertangeChangeRequested(file, percentage / 100);
    }

    @Override
    public void newGetFileRequested(FileItemModel model) {
        controller.getNewFileRequested(model);
    }

    @Override
    public void percentageChangedGetRequested(FileItemModel file, double percentage) {
        controller.getPertangeChangeRequested(file, percentage / 100);
    }

    @Override
    public void ExitRequested() {
        closeActiveConnection();
        if (controller != null) {
            controller.unHookClientObserver(this);
        }
        if (fileServer != null) {
            fileServer.unHookObservers(this);
            fileServer.stopRunning();
        }
        if (fileClient != null) {
            fileClient.stopRunning();
        }
        if (peerDiscovery != null) {
            peerDiscovery.removeObserver(peerListObserver);
            peerDiscovery.stop();
        }
        connected = false;
        notifyAppExitRequested();
    }

    private void closeActiveConnection() {
        try {
            if (fileClient != null && fileClient.hasActiveConnection()) {
                fileClient.closeConnection();
            } else if (fileServer != null && fileServer.hasActiveConnection()) {
                fileServer.closeConnection();
            }
        } catch (IOException ex) {
            Logger.getLogger(PeerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void hookMainAppObserver(IApp client) {
        mainAppObservers.add(client);
    }

    public void unHookMainAppObserver(IApp client) {
        mainAppObservers.remove(client);
    }

    private void notifyAppExitRequested() {
        for (IApp observer : mainAppObservers) {
            observer.exitRequested();
        }
    }

    @Override
    public void connectionFailedRequested() {
        connected = false;
        Platform.runLater(() -> {
            try {
                closeActiveConnection();
                Helpers.AppHelper.restartApplication();
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(PeerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void clientConnectedRequested(String destinationIP) {
        if (connected) {
            return;
        }
        connected = true;
        peerDiscovery.stop();
        Platform.runLater(() -> {
            showMainWindow(TcpIpType.FileServer);
            controller.hookClientObserver(this);
            controller.clientConnected();
            controller.showNotifications("Device (" + destinationIP + ") connected!");
        });
    }

    @Override
    public void serverConnectedRequested(String destinationIP) {
        Platform.runLater(() -> controller.showNotifications("Connected to device (" + destinationIP + ")!"));
    }

    @Override
    public void fileProgressedRequested(int count) {
        Platform.runLater(() -> controller.notifyUserForProgressedFiles(count));
    }

    @Override
    public void threadNeedTimeToFectch() {
        Platform.runLater(() -> controller.showThreadStatus(processedThreadCount.incrementAndGet()));
    }

    @Override
    public void threadCompleteToFectch() {
        Platform.runLater(() -> controller.showThreadStatus(processedThreadCount.decrementAndGet()));
    }

    @Override
    public void sendingStatusChange(boolean isReady) {
        Platform.runLater(() -> controller.setStatusText(isReady));
    }

    @Override
    public void disconnect() {
        closeActiveConnection();
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void beforeStart() {
    }

    @Override
    public void doStart() {
    }

    @Override
    public void afterStart() {
    }
}
