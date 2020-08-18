/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.interfaces;

import com.mycompany.filetransferwithui.models.FileItemModel;
import com.mycompany.filetransferwithui.models.ServerInformation;

/**
 *
 * @author mek
 */
public interface ITcpIpObserver {

    public void newPostFileRequested(FileItemModel model);

    public void percentageChangedPostRequested(FileItemModel file, double percentage);

    public void newGetFileRequested(FileItemModel model);

    public void percentageChangedGetRequested(FileItemModel file, double percentage);

    public void clientConnectedRequested(String destinationIP);

    public void serverConnectedRequested(String destinationIP);

    public void connectionFailedRequested();

    public void fileProgressedRequested(int count); // File Count

    public void threadNeedTimeToFectch();

    public void threadCompleteToFectch();

    public void sendingStatusChange(boolean isReady);
}
