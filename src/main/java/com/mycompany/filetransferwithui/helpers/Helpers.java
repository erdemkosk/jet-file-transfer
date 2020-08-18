/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.helpers;

import com.mycompany.filetransferwithui.MainApp;
import com.mycompany.filetransferwithui.controllers.OsCheck;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javafx.scene.image.Image;
import javax.swing.ImageIcon;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author MEK
 */
public class Helpers {

    public static class NumericHelper {

        public static boolean isNumeric(String str) {
            try {
                double d = Double.parseDouble(str);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }

        public static String convertSecondsToHMmSs(long seconds) {
            long s = seconds % 60;
            long m = (seconds / 60) % 60;
            //long h = (seconds / (60 * 60)) % 24;
            return String.format("%02d:%02d", m, s);
        }

    }

    public static class NetworkHelper {

        public static InetAddress getLocalAddress() {
            try {
                Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
                while (b.hasMoreElements()) {
                    for (InterfaceAddress f : b.nextElement().getInterfaceAddresses()) {
                        if (f.getAddress().isSiteLocalAddress()) {
                            return f.getAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class AppHelper {

        public static void restartApplication() throws URISyntaxException, IOException {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            final File currentJar = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            /* Build command: java -jar application.jar */
            final ArrayList<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        }
    }

    public static class FileAndFolderHelper {

        public static String listDirectory(File parentDirectory, String folderNameToStop) {
            String folders = "";
            while (parentDirectory != null) {
                parentDirectory = parentDirectory.getParentFile();
                if (parentDirectory.getName().equals(folderNameToStop) != true) {
                    if (parentDirectory != null && !parentDirectory.getName().isEmpty()) {

                        folders = "/" + parentDirectory.getName() + folders;
                    }
                } else {
                    break;
                }

            }
            return folders;
        }

        public static List<File> listFilesAndFilesSubDirectories(File directory) {
            List<File> files = (List<File>) FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            return files;
        }
    }

    public static class OperatingSystemHelper {

        public static OsCheck.OSType detectOperatingSystem() {
            OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
            return ostype;
        }

    }
}
