package net.softwarepage.seminararbeit.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

public class NashController implements Initializable {

    @FXML
    private MenuItem newItem;
    @FXML
    private MenuItem openItem;
    @FXML
    private ListView<File> list;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initListContent();
        list.setOnMouseClicked(handler -> {
            if (handler.getClickCount() == 2) {
                NewAndOpenMethods.openGame(list.getSelectionModel().getSelectedItem());
            }
        });

        list.setCellFactory(cf -> new FileCell());
    }

    private void initListContent() {
        String name = "game";
        for (int i = 0; i < 10; i++) {
            String finalName = name + i;
            if (Registry.getFromRegistry(finalName) == null) {
                break;
            }
            list.getItems().add(new File(Registry.getFromRegistry(finalName)));
        }
    }

    @FXML
    private void handleMenuItemClick(ActionEvent event) {
        if (event.getSource().equals(newItem)) {
            NewAndOpenMethods.newGame();
        } else if (event.getSource().equals(openItem)) {
            NewAndOpenMethods.openGame(null);
        }
    }
}
