/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.models;

import java.io.File;

/**
 *
 * @author mek
 */
public class FileItemModel {

    private String fileName;
    private double percentageOfLoadedFile;
    private String time;
    private String transferSpeed;
    private File file;
    private String folderList;

    public String getFolderList() {
        return folderList;
    }

    public void setFolderList(String folderList) {
        this.folderList = folderList;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getTransferSpeed() {
        return transferSpeed;
    }

    public void setTransferSpeed(String transferSpeed) {
        this.transferSpeed = transferSpeed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getPercentageOfLoadedFile() {
        return percentageOfLoadedFile;
    }

    public void setPercentageOfLoadedFile(double percentageOfLoadedFile) {
        this.percentageOfLoadedFile = percentageOfLoadedFile;
    }

    public FileItemModel(String fileName, double percentageOfLoadedFile, String time, String transferSpeed) {
        this.fileName = fileName;
        this.percentageOfLoadedFile = percentageOfLoadedFile;
        this.time = time;
        this.transferSpeed = transferSpeed;
    }

    public FileItemModel(String fileName, double percentageOfLoadedFile, String time, String transferSpeed, File file) {
        this.fileName = fileName;
        this.percentageOfLoadedFile = percentageOfLoadedFile;
        this.time = time;
        this.transferSpeed = transferSpeed;
        this.file = file;
    }

    public FileItemModel(String fileName, double percentageOfLoadedFile, String time, String transferSpeed, File file, String folderList) {
        this.fileName = fileName;
        this.percentageOfLoadedFile = percentageOfLoadedFile;
        this.time = time;
        this.transferSpeed = transferSpeed;
        this.file = file;
        this.folderList = folderList;
    }

}
