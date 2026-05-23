package com.mycompany.filetransferwithui.connections;

import com.mycompany.filetransferwithui.helpers.NetworkHelper;
import com.mycompany.filetransferwithui.interfaces.IPeerDiscoveryObserver;
import com.mycompany.filetransferwithui.models.AppSettings;
import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class PeerDiscoveryService {

    public static final String ANNOUNCE_PREFIX = "PEER_ANNOUNCE>";
    private static final long ANNOUNCE_INTERVAL_MS = 3000;

    private final AppSettings settings;
    private final ArrayList<IPeerDiscoveryObserver> observers = new ArrayList<>();
    private volatile boolean running;
    private Thread listenerThread;
    private Thread announcerThread;

    public PeerDiscoveryService(AppSettings settings) {
        this.settings = settings;
    }

    public void addObserver(IPeerDiscoveryObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(IPeerDiscoveryObserver observer) {
        observers.remove(observer);
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        listenerThread = new Thread(this::listenLoop, "peer-discovery-listener");
        announcerThread = new Thread(this::announceLoop, "peer-discovery-announcer");
        listenerThread.setDaemon(true);
        announcerThread.setDaemon(true);
        listenerThread.start();
        announcerThread.start();
    }

    public void stop() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        if (announcerThread != null) {
            announcerThread.interrupt();
        }
    }

    private void listenLoop() {
        while (running) {
            try (DatagramSocket socket = new DatagramSocket(settings.getDiscoveryServerPortNumber(), InetAddress.getByName("0.0.0.0"))) {
                socket.setBroadcast(true);
                byte[] buffer = new byte[4096];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    handlePacket(packet);
                }
            } catch (IOException ex) {
                if (running) {
                    sleepQuietly(1000);
                }
            }
        }
    }

    private void announceLoop() {
        while (running) {
            broadcastAnnouncement();
            sleepQuietly(ANNOUNCE_INTERVAL_MS);
        }
    }

    private void broadcastAnnouncement() {
        try {
            ServerInformation self = createSelfInformation();
            String payload = ANNOUNCE_PREFIX + gson.toJson(self);
            byte[] data = payload.getBytes();

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                sendBroadcast(socket, data, InetAddress.getByName("255.255.255.255"));
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast != null) {
                            sendBroadcast(socket, data, broadcast);
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void sendBroadcast(DatagramSocket socket, byte[] data, InetAddress address) {
        try {
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    address,
                    settings.getDiscoveryServerPortNumber()
            );
            socket.send(packet);
        } catch (IOException ignored) {
        }
    }

    private void handlePacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength()).trim();
        if (!message.startsWith(ANNOUNCE_PREFIX)) {
            return;
        }

        try {
            ServerInformation peer = gson.fromJson(message.substring(ANNOUNCE_PREFIX.length()), ServerInformation.class);
            if (peer == null || peer.getPeerId() == null) {
                return;
            }
            if (peer.getPeerId().equals(settings.getPeerId())) {
                return;
            }
            if (NetworkHelper.isLocalAddress(packet.getAddress())) {
                return;
            }

            peer.setServerIP(NetworkHelper.normalizeIp(packet.getAddress().getHostAddress()));
            if (peer.getServerPort() <= 0) {
                peer.setServerPort(settings.getPortNumber());
            }
            notifyPeerDiscovered(peer);
        } catch (Exception ignored) {
        }
    }

    private ServerInformation createSelfInformation() {
        try {
            return new ServerInformation(
                    settings.getPeerId(),
                    InetAddress.getLocalHost().getHostName(),
                    settings.getPortNumber()
            );
        } catch (IOException ex) {
            return new ServerInformation(settings.getPeerId(), "JetFileTransfer", settings.getPortNumber());
        }
    }

    private void notifyPeerDiscovered(ServerInformation peer) {
        for (IPeerDiscoveryObserver observer : new ArrayList<>(observers)) {
            observer.onPeerDiscovered(peer);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private final com.google.gson.Gson gson = new com.google.gson.Gson();
}
