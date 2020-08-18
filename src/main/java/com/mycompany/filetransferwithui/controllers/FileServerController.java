/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.controllers;

import com.mycompany.filetransferwithui.connections.BroadcastServer;
import com.mycompany.filetransferwithui.connections.FileServer;
import com.mycompany.filetransferwithui.connections.TcpIpConnectionController;
import com.mycompany.filetransferwithui.enums.TcpIpType;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.interfaces.IApp;
import com.mycompany.filetransferwithui.interfaces.IController;
import com.mycompany.filetransferwithui.interfaces.IMainUIObserver;
import com.mycompany.filetransferwithui.interfaces.ITcpIpObserver;
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
import javafx.stage.Stage;

/**
 *
 * @author mek
 */
public class FileServerController extends TcpIpConnectionController implements IController, ITcpIpObserver, IMainUIObserver {

    private FileServer fileServer;
    private BroadcastServer broadcastServer;
    private ArrayList<IApp> mainAppObservers = new ArrayList<IApp>();
    private BlockingQueue queue = new LinkedBlockingDeque<FileItemModel>();

    public FileServerController(Stage stage) {
        this.stage = stage;

    }

    public void run() {
        beforeStart();
        doStart();
        afterStart();
    }

    @Override
    public void beforeStart() {
        readAndLoadSettings();
        broadcastServer = new BroadcastServer(appSetting);
        broadcastServer.start();

    }

    @Override
    public void doStart() {
        fileServer = new FileServer(appSetting, queue);
        fileServer.hookObservers(this);
        fileServer.start();
    }

    @Override
    public void afterStart() {
        showMainWindow(TcpIpType.FileServer);
        controller.hookClientObserver(this);
    }

    @Override
    public void connectServerRequested(ServerInformation inform) {

    }

    @Override
    public void newFilesTakenRequested(List<File> selectedFiles) {
        new Thread() {
            public void run() {
                try {
                    ArrayList<FileItemModel> models;
                    models = fileServer.generateItemModels(selectedFiles);
                    for (FileItemModel file : models) {
                        fileServer.getFileQueue().put(file);
                        //fileServer.getFileQueue().notifyAll();
                    }

                    fileProgressedRequested(fileServer.getFileQueue().size());

                } catch (IOException ex) {
                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FileClientController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    @Override
    public void newPostFileRequested(FileItemModel model) {
        controller.postNewFileRequested(model);
    }

    @Override
    public void percentageChangedPostRequested(FileItemModel file, double percentage) {
        percentage = percentage / 100;
        controller.postPertangeChangeRequested(file, percentage);
    }

    @Override
    public void newGetFileRequested(FileItemModel model) {
        controller.getNewFileRequested(model);
    }

    @Override
    public void percentageChangedGetRequested(FileItemModel file, double percentage) {
        percentage = percentage / 100;

        controller.getPertangeChangeRequested(file, percentage);
    }

    @Override
    public void ExitRequested() {

        fileServer.unHookObservers(this);
        controller.unHookClientObserver(this);
        fileServer.stopRunning();
        broadcastServer.stopRunning();
        notifyAppExitRequested();
    }

    public void hookMainAppObserver(IApp client) {
        mainAppObservers.add(client);
    }

    public void unHookMainAppObserver(IApp client) {
        mainAppObservers.remove(client);
    }

    private void notifyAppExitRequested() {
        for (int i = 0; i < mainAppObservers.size(); i++) {
            mainAppObservers.get(i).exitRequested();
        }
    }

    @Override
    public void connectionFailedRequested() {

        try {
            Helpers.AppHelper.restartApplication();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FileServerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void clientConnectedRequested(String destinationIP) {
        controller.clientConnected();
        controller.showNotifications("Client(" + destinationIP + ") connected!");

    }

    @Override
    public void serverConnectedRequested(String destinationIP) {

    }

    @Override
    public void fileProgressedRequested(int count) {
        controller.notifyUserForProgressedFiles(count);
    }

    @Override
    public void threadNeedTimeToFectch() {

        controller.showThreadStatus(processedThreadCount.incrementAndGet());
    }

    @Override
    public void threadCompleteToFectch() {

        controller.showThreadStatus(processedThreadCount.decrementAndGet());

    }

    @Override
    public void sendingStatusChange(boolean isReady) {
        controller.setStatusText(isReady);
    }

    @Override
    public void disconnect() {

    }
}
