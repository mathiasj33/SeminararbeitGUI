/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.softwarepage.seminararbeit.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Mathias
 */
public class Seminararbeit extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        NewAndOpenMethods.setStage(stage);
        
        Parent root = FXMLLoader.load(getClass().getResource("Nash.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.lookup("#menubar").requestFocus();
        stage.show();
        
        stage.setOnCloseRequest(e -> Platform.exit());
        
        stage.getIcons().add(new Image(Seminararbeit.class.getResourceAsStream("icon.png")));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
