package com.mycompany.filetransferwithui.interfaces;

import com.mycompany.filetransferwithui.models.ServerInformation;

public interface IPeerDiscoveryObserver {

    void onPeerDiscovered(ServerInformation peer);
}
