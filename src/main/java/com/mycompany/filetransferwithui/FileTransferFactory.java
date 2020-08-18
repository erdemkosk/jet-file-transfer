/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui;

import com.mycompany.filetransferwithui.enums.TcpIpType;
import com.mycompany.filetransferwithui.interfaces.ITcpIp;

/**
 *
 * @author mek
 */
public class FileTransferFactory {

    //use getShape method to get object of type shape 
    public ITcpIp getTcpIpConnection(TcpIpType tcpIpType) {
        ITcpIp connection = null;
        switch (tcpIpType) {
            case FileServer:

                break;
            case FileClient:

                break;

        }
        return connection;
    }
}
