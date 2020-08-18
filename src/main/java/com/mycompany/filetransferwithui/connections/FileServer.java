/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.FileItemModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mek
 */
public class FileServer extends TcpConnections implements Runnable {

    private ServerSocket serverSocket = null;

    public FileServer(AppSettings setting, BlockingQueue<FileItemModel> fileQueue) {
        this.threadName = "FileServerThread";
        this.portNumber = setting.getPortNumber();
        this.folderPath = setting.getSaveFolderPath();
        this.setting = setting;
        generateSocket(portNumber);
        this.fileQueue = fileQueue;

    }

    @Override
    public void waitFilesFromConnection() {
        clientNotification();
        super.waitFilesFromConnection(); //To change body of generated methods, choose Tools | Templates.
    }

    public void start() {

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
        while (isRunning == true) {

            waitFilesFromConnection();
        }

    }

    private void generateSocket(int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber);

        } catch (IOException ex) {

        }
    }

    private void notifyClientConnectedRequested(String destinationIP) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).clientConnectedRequested(destinationIP);
        }
    }

    private void clientNotification() {
        if (socket == null) {
            try {
                socket = serverSocket.accept();
                bis = new BufferedInputStream(socket.getInputStream());
                dis = new DataInputStream(bis);
                bos = new BufferedOutputStream(socket.getOutputStream());
                dos = new DataOutputStream(bos);
                //Karşıdaki clientin ıpsi
            } catch (IOException ex) {
                Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            String clientIP = socket.getRemoteSocketAddress().toString();

            String[] parts = clientIP.split(":");
            String ip = parts[0]; // 004
            ip = ip.replace("/", "");
            notifyClientConnectedRequested(ip);

        }
    }

}
