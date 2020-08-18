/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.models;

import java.util.ArrayList;

/**
 *
 * @author MEK
 */
public class FileTransferModel {

    private String fileName;
    private long fileSize;
    private String folders;

    public FileTransferModel(String fileName, long fileSize, String folders) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.folders = folders;
    }

    public FileTransferModel(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFolders() {
        return folders;
    }

    public void setFolders(String folders) {
        this.folders = folders;
    }

}
