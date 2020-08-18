/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.interfaces;

import com.mycompany.filetransferwithui.models.ServerInformation;

/**
 *
 * @author mek
 */
public interface IBroadcastObserver {

    public void newServerInformationReady(ServerInformation inform);
}
