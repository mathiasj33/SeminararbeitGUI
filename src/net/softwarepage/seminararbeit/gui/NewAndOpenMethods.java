package net.softwarepage.seminararbeit.gui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.softwarepage.facharbeit.normalgame.helpers.FileManager;
import net.softwarepage.facharbeit.normalgame.logic.NormalGame;

public class NewAndOpenMethods {

    private static Stage stage;
    private static FXMLLoader loader = new FXMLLoader(NewAndOpenMethods.class.getResource("NormalGame.fxml"));

    public static void setStage(Stage stage) {
        NewAndOpenMethods.stage = stage;
    }

    public static void newGame() {
        loader = new FXMLLoader(NewAndOpenMethods.class.getResource("NormalGame.fxml"));
        loader.setController(new NormalGameController());
        swapScenes();
    }

    public static void openGame(File file) {
        try {
            loader = new FXMLLoader(NewAndOpenMethods.class.getResource("NormalGame.fxml"));
            if (file == null) {
                file = FileManager.createLoadingFileChooser().showOpenDialog(stage);
            }
            NormalGame game = FileManager.loadGame(file);
            NormalGameController ngc = new NormalGameController(game);
            loader.setController(ngc);
            swapScenes();
            Registry.saveToRegistry(file.getAbsolutePath());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(NewAndOpenMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void swapScenes() {
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.lookup("#menubar").requestFocus();
            stage.show();
            stage.setOnCloseRequest(e -> Platform.exit());
        } catch (IOException ex) {
            Logger.getLogger(NashController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
