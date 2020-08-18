/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author mek
 */
public interface ITcpIp {

	//public void sendFileToConnection(List<File> fileList) throws IOException;
	public void waitFilesFromConnection();
}
