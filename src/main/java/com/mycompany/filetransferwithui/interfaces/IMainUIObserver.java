/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.interfaces;

import com.mycompany.filetransferwithui.models.ServerInformation;
import java.io.File;
import java.util.List;

/**
 *
 * @author mek
 */
public interface IMainUIObserver {

    public void connectServerRequested(ServerInformation inform);

    public void newFilesTakenRequested(List<File> selectedFiles);

    public void ExitRequested();

}
