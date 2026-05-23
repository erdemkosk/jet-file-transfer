package com.mycompany.filetransferwithui.helpers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class NetworkHelper {

    private NetworkHelper() {
    }

    public static Set<String> getLocalHostAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            addresses.add(InetAddress.getLocalHost().getHostAddress());
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address) {
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return addresses;
    }

    public static boolean isLocalAddress(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isLoopbackAddress()) {
            return true;
        }
        String hostAddress = address.getHostAddress();
        if (hostAddress == null) {
            return true;
        }
        return getLocalHostAddresses().contains(hostAddress);
    }

    public static String normalizeIp(String ip) {
        if (ip == null) {
            return "";
        }
        return ip.startsWith("/") ? ip.substring(1) : ip;
    }
}
