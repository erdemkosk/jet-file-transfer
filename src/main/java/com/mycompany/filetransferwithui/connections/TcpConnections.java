/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.google.gson.Gson;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.interfaces.ITcpIp;
import com.mycompany.filetransferwithui.interfaces.ITcpIpObserver;
import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.FileItemModel;
import com.mycompany.filetransferwithui.models.FileTransferModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mustafa Erdem Köşk<erdemkosk@gmail.com>
 */
public class TcpConnections implements ITcpIp {

    protected long totalTransferedBytesInoneSecondSend = 0;
    protected long totalTransferedBytesInoneSecondGet = 0;
    protected int countSend = 0;
    protected int countGet = 0;
    protected long deltaTimeGet = 0;
    protected long deltaTimePost = 0;
    protected Timer timerGet;
    protected Timer timerPost;
    protected Thread thread;
    protected String threadName;
    protected Socket socket = null;
    protected AppSettings setting;
    protected String folderPath;
    protected boolean isRunning = true;
    protected int portNumber;
    protected ArrayList<ITcpIpObserver> observers = new ArrayList<ITcpIpObserver>();
    protected BlockingQueue<FileItemModel> fileQueue;
    protected BufferedInputStream bis;
    protected DataInputStream dis;
    protected BufferedOutputStream bos;
    protected DataOutputStream dos;
    protected Gson gson = new Gson();

    protected void runTimerGet(FileItemModel model) {
        timerGet = new Timer();

        timerGet.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                deltaTimeGet++;

                long transferSpeed = totalTransferedBytesInoneSecondGet;
                totalTransferedBytesInoneSecondGet = 0;

                model.setTime(Helpers.NumericHelper.convertSecondsToHMmSs(deltaTimeGet));
                model.setTransferSpeed(FileUtils.byteCountToDisplaySize(transferSpeed).toLowerCase(Locale.ENGLISH) + "/s");
            }
        }, 0, 1000);

    }

    protected void stopTimerGet() {

        deltaTimeGet = 0;
        timerGet.cancel();
    }

    protected void runTimerPost(FileItemModel model) {
        timerPost = new Timer();

        timerPost.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                deltaTimePost++;

                long transferSpeed = totalTransferedBytesInoneSecondSend;
                totalTransferedBytesInoneSecondSend = 0;

                model.setTime(Helpers.NumericHelper.convertSecondsToHMmSs(deltaTimePost));
                model.setTransferSpeed(FileUtils.byteCountToDisplaySize(transferSpeed).toLowerCase(Locale.ENGLISH) + "/s");
            }
        }, 0, 1000);

    }

    protected void stopTimerPost() {

        deltaTimePost = 0;
        timerPost.cancel();
    }

    public ArrayList<FileItemModel> generateItemModels(List<File> fileList) throws IOException {

        ArrayList<FileItemModel> models = new ArrayList<>();
        ArrayList<File> folders = new ArrayList<>();

        notifyThreadNeedTimeToFectchRequested(); // threde zaman lazım diyorum

        for (File file : fileList) {
            if (file.isDirectory() == true) { // Eleminate folders
                folders.add(file);
                continue;
            }
            String name = file.getName();

            FileItemModel model = new FileItemModel(name, 0, "00:00", "Waiting", file);
            notifyPostObserversNewFileRequested(model);
            models.add(model);
        }
        for (File folder : folders) {

            List<File> folderInside = Helpers.FileAndFolderHelper.listFilesAndFilesSubDirectories(folder); // folderin içinde ki tüm filelear
            for (File files : folderInside) {

                String folderNames = Helpers.FileAndFolderHelper.listDirectory(files, folder.getParentFile().getName());
                FileItemModel model = new FileItemModel(files.getName(), 0, "00:00", "Waiting", files, folderNames);
                notifyPostObserversNewFileRequested(model);
                models.add(model);
            }

        }
        notifyThreadCompleteToFectchRequested(); // threde işi bitti
        return models;
    }

    public void sendFileToConnection() throws IOException {
        new Thread() {
            public void run() {
                while (isRunning == true) {
                    try {
                        FileItemModel model = fileQueue.take();

                        if (model == null) {
                            continue;
                        }

                        try {
                            notifyObserversFileProgressed(fileQueue.size());
                            String name = model.getFileName();
                            long length = model.getFile().length();
                            FileTransferModel transferModel;
                            if (model.getFolderList() != null) { // yaratılması gereken klasörleri varsa
                                transferModel = new FileTransferModel(name, length, model.getFolderList());
                            } else {
                                transferModel = new FileTransferModel(name, length);
                            }

                            String readedJson = gson.toJson(transferModel);
                            dos.writeUTF(readedJson);
                            dos.flush();
                            runTimerPost(model);
                            //notifyNewFileRequested(model);

                            FileInputStream fis = new FileInputStream(model.getFile());

                            //buffer for file writing, to declare inside or outside loop?
                            int n = 0;
                            double pertange = 0;
                            double totalRead = 0;

                            byte[] buf = new byte[4096];
                            notifySendingStatusChangeRequested(false);
                            while ((n = fis.read(buf)) != -1) {
                                bos.write(buf, 0, n);
                                bos.flush();
                                totalRead += n;
                                totalTransferedBytesInoneSecondSend += n;
                                pertange = (totalRead * 100) / length;

                                int pertangeFormated = Integer.parseInt(new DecimalFormat("###").format(pertange));
                                countSend++;
                                if (countSend % 5 == 0 || pertangeFormated == 100) {
                                    notifyPostObserversPercentageChanged(model, pertangeFormated);
                                    countSend = 0;
                                }

                            }
                            model.setTransferSpeed("Completed!");
                            stopTimerPost();
                            totalTransferedBytesInoneSecondSend = 0;
                            notifySendingStatusChangeRequested(true);
                        } catch (IOException ex) {
                            Logger.getLogger(TcpConnections.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (Exception ex) {

                        Logger.getLogger(TcpConnections.class.getName()).log(Level.SEVERE, null, ex);
                        notifyConnectionCloseRequested();
                    }
                }
            }
        }.start();

    }

    public BlockingQueue<FileItemModel> getFileQueue() {
        return fileQueue;
    }

    public void waitFilesFromConnection() {

        try {

            String json = dis.readUTF();
//            notifyGetObserversNewFileRequested(fileItem); //notify

            FileTransferModel transferModel = gson.fromJson(json, FileTransferModel.class);

            FileItemModel fileItem = new FileItemModel(transferModel.getFileName(), 0, "00:00", "");
            runTimerGet(fileItem);

            notifyGetObserversNewFileRequested(fileItem); //notify
            File savedfile = null;
            if (transferModel.getFolders() != null) {
                Path path = Paths.get(folderPath + transferModel.getFolders());

                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    System.err.println("Cannot create directories - " + e);
                }

                System.out.println(transferModel.getFolders());
                savedfile = new File(folderPath + transferModel.getFolders() + "/" + transferModel.getFileName());

            } else {
                savedfile = new File(folderPath + "/" + transferModel.getFileName());
            }
            if (savedfile != null) {

                FileOutputStream fos = new FileOutputStream(savedfile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                double pertange = 0;
                double totalRead = 0;

                int n = 0;
                byte[] buf = new byte[4096];
                long fileSize = transferModel.getFileSize();
                while (fileSize > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                    fos.write(buf, 0, n);
                    fileSize -= n;
                    totalRead += n;
                    totalTransferedBytesInoneSecondGet += n;
                    pertange = (totalRead * 100) / transferModel.getFileSize();
                    int pertangeFormated = Integer.parseInt(new DecimalFormat("###").format(pertange));

                    notifyGetObserversPercentageChanged(fileItem, pertangeFormated);

                }
                fileItem.setTransferSpeed("Completed!");
                stopTimerGet();
                totalTransferedBytesInoneSecondGet = 0;
                bos.close();
            }

//dis.close();}
        } catch (Exception ex) {

            Logger.getLogger(TcpConnections.class.getName()).log(Level.SEVERE, null, ex);
            notifyConnectionCloseRequested();
        }
    }
    //dis.close();

    public void closeConnection() throws IOException {
        isRunning = false;
        dos.close();
        bos.close();
        bis.close();
        dis.close();
        socket.close();
    }

    public void hookObservers(ITcpIpObserver observer) {
        observers.add(observer);
    }

    public void unHookObservers(ITcpIpObserver observer) {
        observers.remove(observer);
    }

    protected void notifyGetObserversNewFileRequested(FileItemModel file) {
        for (ITcpIpObserver temp : observers) {
            temp.newGetFileRequested(file);
        }
    }

    protected void notifyGetObserversPercentageChanged(FileItemModel file, double pertange) {
        for (ITcpIpObserver temp : observers) {
            temp.percentageChangedGetRequested(file, pertange);
        }
    }

    protected void notifyPostObserversPercentageChanged(FileItemModel file, double pertange) {
        for (ITcpIpObserver temp : observers) {
            temp.percentageChangedPostRequested(file, pertange);
        }
    }

    protected void notifyObserversFileProgressed(int count) {
        for (ITcpIpObserver temp : observers) {
            temp.fileProgressedRequested(count);
        }
    }

    protected void notifyPostObserversNewFileRequested(FileItemModel file) {
        for (ITcpIpObserver temp : observers) {
            temp.newPostFileRequested(file);
        }
    }

    protected void notifyConnectionCloseRequested() {
        try {
            closeConnection();
        } catch (IOException ex) {
            Logger.getLogger(TcpConnections.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (ITcpIpObserver temp : observers) {
            temp.connectionFailedRequested();
        }
    }

    protected void notifyServerConnectedRequested(String destinationIP) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).serverConnectedRequested(destinationIP);
        }
    }

    protected void notifyNewFileRequested(FileItemModel model) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).newGetFileRequested(model);
        }
    }

    protected void notifyThreadNeedTimeToFectchRequested() {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).threadNeedTimeToFectch();
        }
    }

    protected void notifyThreadCompleteToFectchRequested() {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).threadCompleteToFectch();
        }
    }

    protected void notifySendingStatusChangeRequested(boolean isReady) {
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).sendingStatusChange(isReady);
        }
    }

    public AppSettings getSetting() {
        return setting;
    }

    public void setSetting(AppSettings setting) {
        this.setting = setting;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void stopRunning() {
        isRunning = false;
    }

}
