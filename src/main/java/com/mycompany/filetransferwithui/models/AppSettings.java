/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.models;

/**
 *
 * @author mek
 */
public class AppSettings {

	private int portNumber;
	private String saveFolderPath;
	private int discoveryServerPortNumber;
	private boolean showTrayNotification;

	public boolean getShowTrayNotification() {
		return showTrayNotification;
	}

	public void setShowTrayNotification(boolean showTrayNotification) {
		this.showTrayNotification = showTrayNotification;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getSaveFolderPath() {
		return saveFolderPath;
	}

	public void setSaveFolderPath(String saveFolderPath) {
		this.saveFolderPath = saveFolderPath;
	}

	public int getDiscoveryServerPortNumber() {
		return discoveryServerPortNumber;
	}

	public void setDiscoveryServerPortNumber(int discoveryServerPortNumber) {
		this.discoveryServerPortNumber = discoveryServerPortNumber;
	}

	public AppSettings(int portNumber, String saveFolderPath, int discoveryServerPortNumber, boolean showTrayNotification) {
		this.portNumber = portNumber;
		this.saveFolderPath = saveFolderPath;
		this.discoveryServerPortNumber = discoveryServerPortNumber;
		this.showTrayNotification = showTrayNotification;
	}

}
