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

	private String peerId;
	private String serverIP;
	private int serverPort;
	private String serverHostName;

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

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

	public ServerInformation(String peerId, String serverHostName, int serverPort) {
		this.peerId = peerId;
		this.serverHostName = serverHostName;
		this.serverPort = serverPort;
	}

	@Override
	public String toString() {
		String name = serverHostName != null ? serverHostName : "Unknown device";
		String ip = serverIP != null && !serverIP.isEmpty() ? " (" + serverIP + ")" : "";
		return name + ip;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ServerInformation)) {
			return false;
		}
		ServerInformation other = (ServerInformation) obj;
		if (peerId != null && other.peerId != null) {
			return peerId.equals(other.peerId);
		}
		return serverIP != null && serverIP.equals(other.serverIP) && serverPort == other.serverPort;
	}

	@Override
	public int hashCode() {
		if (peerId != null) {
			return peerId.hashCode();
		}
		return serverPort ^ (serverIP != null ? serverIP.hashCode() : 0);
	}
}
