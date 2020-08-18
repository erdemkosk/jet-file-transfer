/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.google.gson.Gson;
import com.mycompany.filetransferwithui.models.AppSettings;
import java.net.DatagramSocket;

/**
 *
 * @author MEK
 */
public class UdpConnections {

    DatagramSocket socket;
    protected Thread thread;
    protected String threadName;
    protected int portNumber;
    protected AppSettings setting;
    protected boolean isRunning = true;
    protected Gson gson = new Gson();

    public void stopRunning() {
        isRunning = false;
    }
}
