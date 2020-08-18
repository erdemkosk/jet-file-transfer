/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.filetransferwithui.models;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;

/**
 *
 * @author mek
 */
public class FileListViewCell extends ListCell<FileItemModel> {
	
	@FXML
	private Label labelName;
	@FXML
	private Label lblTime;
	@FXML
	private Label lblSpeed;
	@FXML
	private GridPane gridPane;
	
	@FXML
	private ProgressIndicator progress;
	
	private FXMLLoader mLLoader;
	
	@Override
	protected void updateItem(FileItemModel file, boolean empty) {
		super.updateItem(file, empty);
		
		if (empty || file == null) {
			
			setText(null);
			setGraphic(null);
			
		} else {
			if (mLLoader == null) {
				mLLoader = new FXMLLoader(getClass().getResource("/fxml/ListCell.fxml"));
				mLLoader.setController(this);
				
				try {
					mLLoader.load();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			labelName.setText(String.valueOf(file.getFileName()));
			progress.setProgress(file.getPercentageOfLoadedFile());
			lblTime.setText(file.getTime());
			lblSpeed.setText(file.getTransferSpeed());
			setText(null);
			setGraphic(gridPane);
		}
		
	}
}
