package net.softwarepage.seminararbeit.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.softwarepage.facharbeit.normalgame.logic.NormalGame;

public class EditController implements Initializable {

    private final NormalGame game;
    private final NormalGameController ngc;
    private final Stage editStage;

    @FXML
    private Button applyRows;
    @FXML
    private TextField rowField;
    @FXML
    private Button applyColumns;
    @FXML
    private TextField columnField;
    @FXML
    private Button finishButton;

    public EditController(NormalGame game, NormalGameController ngc, Stage editStage) {
        this.game = game;
        this.ngc = ngc;
        this.editStage = editStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        finishButton.setOnAction(e -> {
            if (!rowField.getText().equals("")) {
                applyRows.fire();
            }
            if (!columnField.getText().equals("")) {
                applyColumns.fire();
            }
            finishButton.fireEvent(new WindowEvent(editStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        rowField.setText(String.valueOf(game.getPlayer1().getStrategies().size()));
        columnField.setText(String.valueOf(game.getPlayer2().getStrategies().size()));

    }

    @FXML
    private void handleButtonClick(ActionEvent event) {
        if (event.getSource().equals(applyRows)) {
            int rowCount = Integer.parseInt(rowField.getText());
            if (rowCount == 0) {
                return;
            }
            ngc.setNumberOfStrategies(GUIStrategyType.Row, rowCount);
        } else if (event.getSource().equals(applyColumns)) {
            int columnCount = Integer.parseInt(columnField.getText());
            if (columnCount == 0) {
                return;
            }
            ngc.setNumberOfStrategies(GUIStrategyType.Column, columnCount);
        }
    }
}
