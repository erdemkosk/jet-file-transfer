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
public class ServerInformation {

	private String serverIP;
	private int serverPort;
	private String serverHostName;

	public String getServerHostName() {
		return serverHostName;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public ServerInformation(String serverIP, int serverPort, String serverHostName) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.serverHostName = serverHostName;
	}

	public ServerInformation(String serverHostName, int serverPort) {

		this.serverHostName = serverHostName;
		this.serverPort = serverPort;
	}

	@Override
	public String toString() {
		return serverHostName + serverIP + ":" + serverPort;
	}

}
