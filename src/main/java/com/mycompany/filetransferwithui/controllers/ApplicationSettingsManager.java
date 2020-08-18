/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.controllers;

import com.google.gson.Gson;
import com.mycompany.filetransferwithui.MainApp;
import com.mycompany.filetransferwithui.helpers.Helpers;
import com.mycompany.filetransferwithui.models.AppSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.scene.image.Image;
import javax.swing.ImageIcon;

/**
 *
 * @author mek
 */
public class ApplicationSettingsManager {

	private String path;
	private Gson gson;
        
         private void checkAndReadyPath() {
        OsCheck.OSType ostype = Helpers.OperatingSystemHelper.detectOperatingSystem();
        switch (ostype) {
            case Windows:
                //stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/app.png")));
               path = new File("").getAbsolutePath();
                break;
            case MacOS:
                 path =  System.getProperty("user.home");
                break;
            case Linux:
                path = new File("").getAbsolutePath();
                break;
            case Other:
                path = new File("").getAbsolutePath();
                break;
        }
    }

	public ApplicationSettingsManager() {
		gson = new Gson();
		checkAndReadyPath();
		System.out.println(path);
	}

	private void generateDefaultConfig() {

		AppSettings settings = new AppSettings(4444, path, 8888, true); //File Server Port , SavePath , DiscoveryServerPort,Show Tray Not
		String json = gson.toJson(settings);
		try {
			PrintWriter writer = new PrintWriter(path + "/settings.jft", "UTF-8");
			writer.println(json);
			writer.close();
		} catch (IOException e) {

		}
	}

	public AppSettings readDefaultConfigIfDoesNotExitCreateOne() throws IOException {
		AppSettings settings;
		File f = new File(path + "/settings.jft");
		if (f.exists() == false) {
			generateDefaultConfig();
		}
		String content = "";

		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(path + "/settings.jft"), "UTF8"));

		String str;

		while ((str = in.readLine()) != null) {
			content += str;
		}

		in.close();

		settings = gson.fromJson(content, AppSettings.class);

		return settings;
	}

	public void writeConfigSettingsDeleteOldOne(AppSettings settings) {
		File file = new File(path + "/settings.jft");
		file.delete();
		String json = gson.toJson(settings);
		try {
			PrintWriter writer = new PrintWriter(path + "/settings.jft", "UTF-8");
			writer.println(json);
			writer.close();
		} catch (IOException e) {

		}
	}

}
