/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.connections;

import com.mycompany.filetransferwithui.interfaces.IBroadcastObserver;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author mek
 */
public class BroadcastClient extends UdpConnections implements Runnable {

    private ArrayList<IBroadcastObserver> observers = new ArrayList<IBroadcastObserver>();

    @Override
    public void run() {
        // Find the server using UDP broadcast
        while (isRunning == true) {

            try {

                //Open a random port to send the package
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();
                //Try the 255.255.255.255 first
                try {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                    socket.send(sendPacket);
                } catch (Exception e) {
                }
                // Broadcast the message over all the network interfaces
                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }
                        // Send the broadcast package!
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                            socket.send(sendPacket);
                        } catch (Exception e) {
                        }
                    }
                }
                socket.setSoTimeout(10000);
                //Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(receivePacket);
                //We have a response
                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                if (message.contains("DISCOVER_FUIFSERVER_RESPONSE")) {
                    System.out.println(message);
                    String[] parts = message.split(">");

                    ServerInformation information = gson.fromJson(parts[1], ServerInformation.class);

                    information.setServerIP(receivePacket.getAddress().toString());
                    notifyObserversnewServerReady(information);
                    isRunning = false;
                }
                //Close the port!
                socket.close();
            } catch (IOException ex) {

            }
        }
    }

    public void hookObserver(IBroadcastObserver observer) {
        observers.add(observer);

    }

    public void start() {

        if (thread == null) {
            thread = new Thread(this, "broadcastThread");
            thread.start();
        }
    }

    public void unHookObserver(IBroadcastObserver observer) {
        observers.remove(observer);

    }

    private void notifyObserversnewServerReady(ServerInformation inform) {
        for (IBroadcastObserver temp : observers) {
            temp.newServerInformationReady(inform);

        }
    }

}
