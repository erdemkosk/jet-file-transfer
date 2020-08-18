/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.interfaces;

/**
 *
 * @author mek
 */
public interface IController extends IControllerTemplate {

    public void run();

    public void hookMainAppObserver(IApp client);

    public void unHookMainAppObserver(IApp client);

    public void disconnect();

}
