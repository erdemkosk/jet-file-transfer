/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.FileItemModel;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author mek
 */
public class FileClient extends TcpConnections implements Runnable {

    private boolean isConnected = false;

    public FileClient(AppSettings setting, BlockingQueue<FileItemModel> fileQueue) {
        this.setting = setting;
        this.threadName = "ClientServerThread";
        this.portNumber = setting.getPortNumber();
        this.folderPath = setting.getSaveFolderPath();
        this.fileQueue = fileQueue;
    }

    public void connectToServer(ServerInformation inform) throws IOException {

        socket = new Socket(inform.getServerIP(), inform.getServerPort());
        bis = new BufferedInputStream(socket.getInputStream());
        dis = new DataInputStream(bis);
        bos = new BufferedOutputStream(socket.getOutputStream());
        dos = new DataOutputStream(bos);
        String serverIP = socket.getRemoteSocketAddress().toString();

        String[] parts = serverIP.split(":");
        String ip = parts[0]; // 004
        ip = ip.replace("/", "");
        notifyServerConnectedRequested(ip);
        isConnected = true;

        if (thread == null) {
            thread = new Thread(this, threadName);
            thread.start();
        }

    }

    @Override
    public void run() {
        try {
            sendFileToConnection();
        } catch (IOException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (isRunning) {
            waitFilesFromConnection();
        }
    }

    public boolean isIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

}
