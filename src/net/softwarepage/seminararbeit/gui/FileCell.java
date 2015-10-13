package net.softwarepage.seminararbeit.gui;

import java.io.File;
import javafx.scene.control.ListCell;

public class FileCell extends ListCell<File> {
    @Override
    public void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        setText(item.getName());
    }
}
