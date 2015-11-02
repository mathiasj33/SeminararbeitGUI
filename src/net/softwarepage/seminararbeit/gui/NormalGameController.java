package net.softwarepage.seminararbeit.gui;

import customcontrols.VectorField;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.softwarepage.facharbeit.normalgame.helpers.FileManager;
import net.softwarepage.facharbeit.normalgame.logic.MixedNashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.NashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.PureNashEquilibrium;
import net.softwarepage.facharbeit.normalgame.logic.NormalGame;
import net.softwarepage.facharbeit.normalgame.logic.Player;
import net.softwarepage.facharbeit.normalgame.logic.Strategy;
import net.softwarepage.facharbeit.normalgame.logic.StrategyPair;
import net.softwarepage.facharbeit.normalgame.logic.Vector;

/**
 *
 * @author Mathias
 */
public class NormalGameController implements Initializable {

    @FXML
    private MenuBar menubar;
    @FXML
    private MenuItem newItem;
    @FXML
    private MenuItem open;
    @FXML
    private MenuItem addRow;
    @FXML
    private MenuItem addColumn;
    @FXML
    private MenuItem removeRow;
    @FXML
    private MenuItem removeColumn;
    @FXML
    private MenuItem editMatrix;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane gridAnchor;
    @FXML
    private GridPane grid;
    @FXML
    private ListView<NashEquilibrium> list;

    @FXML
    private MenuItem findDominatedStrategies;
    @FXML
    private MenuItem findCleanNash;
    @FXML
    private MenuItem findMixedNash;

    @FXML
    private MenuItem save;

    private Stage editStage;
    private String currentFocusedText = null;
    private Node oldSelection;
    private boolean propertiesOpened;
    private boolean showingOptimalStrategies;

    private Stage loadingStage;

    private final NormalGame game;

    public NormalGameController() {
        game = new NormalGame();
    }

    public NormalGameController(NormalGame game) {
        this.game = game;
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        if (grid == null) {
            return;
        }
        setupActionHandlers();
        initListView();
        refreshGUI();
        setupContextMenus();

        gridAnchor.prefWidthProperty().bind(scrollPane.prefWidthProperty());
        gridAnchor.prefHeightProperty().bind(scrollPane.prefHeightProperty());
    }

    private void setupActionHandlers() {
        for (Node n : grid.getChildren()) {
            if (n instanceof VectorField) {
                VectorField vf = (VectorField) n;
                vf.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
                    vectorFieldFocusChanged(vf, arg0, oldPropertyValue, newPropertyValue);
                });
                vf.setOnAction(e -> grid.requestFocus());
            } else if (n instanceof TextField) {
                TextField tf = (TextField) n;
                tf.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
                    textFieldFocusChanged(tf, arg0, oldPropertyValue, newPropertyValue);
                });
                tf.setOnAction(e -> grid.requestFocus());
            }
        }
    }

    private void setupContextMenus() {
        for (Strategy strat : game.getPlayer1().getStrategies()) {
            TextField tf = (TextField) getStrategyField(strat, game.getPlayer1());
            if (tf == null) {
                return;
            }
            tf.setContextMenu(createStrategyContextMenu(strat));
        }
        for (Strategy strat : game.getPlayer2().getStrategies()) {
            TextField tf = (TextField) getStrategyField(strat, game.getPlayer2());
            if (tf == null) {
                return;
            }
            tf.setContextMenu(createStrategyContextMenu(strat));
        }
    }

    private ContextMenu createStrategyContextMenu(Strategy strat) {
        ContextMenu cm = new ContextMenu();
        MenuItem item = new MenuItem("Strategie löschen");
        item.setOnAction(e -> {
            unColorAllStrategyFields(game.getPlayer1());
            unColorAllStrategyFields(game.getPlayer2());
            removeStrategy(strat);
        });
        cm.getItems().add(item);
        return cm;
    }

    private void initListView() {
        list.setPlaceholder(new Label("Keine Nash-Gleichgewichte"));
        list.setCellFactory(n -> new NashEquilibriumCell(game));
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.getSelectionModel().selectedItemProperty().addListener(l -> {
            if (oldSelection != null) {
                unColor(oldSelection);
            }
            NashEquilibrium equilibrium = (NashEquilibrium) list.getSelectionModel().getSelectedItem();
            if (equilibrium == null) {
                return;
            }
            if (equilibrium instanceof PureNashEquilibrium) {
                PureNashEquilibrium nne = (PureNashEquilibrium) equilibrium;
                try {
                    Node field = getField(nne.getFirstStrat(), nne.getSecondStrat());
                    oldSelection = field;
                    paintGreen(field);
                } catch (NullPointerException npe) {
                }
            } else {
                if (!showingOptimalStrategies) {
                    return;
                } else {
                    removeGUIStrategy(GUIStrategyType.Row);
                    removeGUIStrategy(GUIStrategyType.Column);
                    setupOptimalStrategies((MixedNashEquilibrium) equilibrium);
                }
                Node field = getNodeByRowColumnIndex(getRowCount() - 2, getColumnCount() - 2);
                oldSelection = field;
                paintGreen(field);
            }
        });
    }

    private void refreshGUI() {
        resetPlayerName(game.getPlayer1());
        resetPlayerName(game.getPlayer2());

        addStrategies(game.getPlayer1());
        addStrategies(game.getPlayer2());

        resetVectors();
    }

    private void resetPlayerName(Player player) {
        TextField field;
        if (player.equals(game.getPlayer1())) {
            field = (TextField) getNodeByRowColumnIndex(2, 0);
        } else {
            field = (TextField) getNodeByRowColumnIndex(0, 2);
        }
        field.setText(player.getName());
    }

    private void addStrategies(Player player) {
        for (Strategy strat : player.getStrategies()) {
            if (getStrategyField(strat, player) == null) {
                if (player.equals(game.getPlayer1())) {
                    addNormalStrategy(GUIStrategyType.Row, strat.getName());
                } else {
                    addNormalStrategy(GUIStrategyType.Column, strat.getName());
                }
            } else {
                TextField field = (TextField) getStrategyField(strat, player);
                field.setText(strat.getName());
            }
        }
    }

    private void resetVectors() {
        for (StrategyPair pair : game.getVectors().keySet()) {
            VectorField field = (VectorField) getField(pair.getStrategy1(), pair.getStrategy2());
            float[] values = field.getValues();
            Vector vector = game.getVectors().get(pair);
            Vector guiVector = new Vector(values[0], values[1]);
            if (!guiVector.equals(vector)) {
                DecimalFormat df = new DecimalFormat("#.###");
                df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
                field.setText(df.format(vector.getFirst()) + ";" + df.format(vector.getSecond()));
            }
        }
    }

    @FXML
    private void handleFileClick(ActionEvent event) {
        menubar.requestFocus();
        if (event.getSource().equals(newItem)) {
            NewAndOpenMethods.newGame();
        } else if (event.getSource().equals(open)) {
            NewAndOpenMethods.openGame(null);
        } else if (event.getSource().equals(save)) {
            FileChooser fc = FileManager.createSavingFileChooser();
            File file = fc.showSaveDialog(menubar.getScene().getWindow());
            if (file == null) {
                return;
            }
            saveGame(file);
        }
    }

    private void hidePossibleOptimalStrategies() {
        if (showingOptimalStrategies) {
            showingOptimalStrategies = false;
            removeGUIStrategy(GUIStrategyType.Row);
            removeGUIStrategy(GUIStrategyType.Column);
        }
    }

    @FXML
    private void handleEditClick(ActionEvent event) {
        menubar.requestFocus();
        hidePossibleOptimalStrategies();
        if (event.getSource().equals(addRow)) {
            addNormalStrategy(GUIStrategyType.Row, null);
        } else if (event.getSource().equals(addColumn)) {
            addNormalStrategy(GUIStrategyType.Column, null);
        } else if (event.getSource().equals(editMatrix)) {
            openEditScene();
        } else if (event.getSource().equals(removeRow)) {
            unColorAllStrategyFields(game.getPlayer1());
            unColorAllStrategyFields(game.getPlayer2());
            removeStrategy(GUIStrategyType.Row);
        } else if (event.getSource().equals(removeColumn)) {
            unColorAllStrategyFields(game.getPlayer1());
            unColorAllStrategyFields(game.getPlayer2());
            removeStrategy(GUIStrategyType.Column);
        }
    }

    @FXML
    private void handleActionClick(ActionEvent event) {
        menubar.requestFocus();
        hidePossibleOptimalStrategies();
        if (event.getSource().equals(findDominatedStrategies)) {
            unColorAllStrategyFields(game.getPlayer1());
            unColorAllStrategyFields(game.getPlayer2());
            List<Strategy> firstDominatedStrategies = game.findDominatedStrategies(game.getPlayer1());
            List<Strategy> secondDominatedStrategies = game.findDominatedStrategies(game.getPlayer2());
            if (!firstDominatedStrategies.isEmpty())
                changeAppearance(firstDominatedStrategies, game.getPlayer1());
            if (!secondDominatedStrategies.isEmpty())
                changeAppearance(secondDominatedStrategies, game.getPlayer2());
        } else if (event.getSource().equals(findCleanNash)) {
            List<NashEquilibrium> nes = new ArrayList<>();
            nes.addAll(game.findPureNashEquilibria());
            setListViewContent(nes);
        } else if (event.getSource().equals(findMixedNash)) {
            showLoadingStage();
            Thread t = new Thread(() -> {
                System.out.println(game.findMixedNashEquilibria());
                List<MixedNashEquilibrium> nes = game.findMixedNashEquilibria();
                if (nes == null) {
                    Platform.runLater(() -> {
                        loadingStage.close();
                    });
                    return;
                }

                Platform.runLater(() -> {
                    setListViewContent(nes);
                    setupOptimalStrategies(nes.get(0));
                    loadingStage.close();
                });
            });
            t.start();
        }
    }

    private void saveGame(File file) {
        try {
            if (!file.getName().endsWith(".game")) {
                file = FileManager.getCorrectedFile(file);
            }
            FileManager.saveGame(file, game);
            Registry.saveToRegistry(file.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(NormalGameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showLoadingStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Calculating.fxml"));
            Scene scene = new Scene(loader.load());
            loadingStage = new Stage();
            loadingStage.setResizable(false);
            loadingStage.initOwner(menubar.getScene().getWindow());
            loadingStage.getIcons().add(new Image(Seminararbeit.class.getResourceAsStream("icon.png")));
            loadingStage.setScene(scene);
            loadingStage.setOnCloseRequest(e -> e.consume());
            loadingStage.setAlwaysOnTop(true);
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.show();
        } catch (IOException ex) {
            Logger.getLogger(NormalGameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void unColorAllStrategyFields(Player player) {
        for (Strategy strategy : player.getStrategies()) {
            unColor(getStrategyField(strategy, player));
        }
    }

    private void changeAppearance(List<Strategy> strategies, Player player) {
        strategies.forEach((strat) -> paintRed(getStrategyField(strat, player)));
    }

    private void paintRed(Node n) {
        n.setStyle("-fx-background-color: firebrick; -fx-text-fill: white;");
    }

    private void paintGreen(Node node) {
        node.setStyle("-fx-background-color: limegreen;");
    }

    private void unColor(Node node) {
        node.setStyle(null);
    }

    private void setupOptimalStrategies(MixedNashEquilibrium mne) {
        addOptimalStrategy(GUIStrategyType.Row, "Optimal gemischt", mne);
        addOptimalStrategy(GUIStrategyType.Column, "Optimal gemischt", mne);
        showingOptimalStrategies = true;
    }

    public void setNumberOfStrategies(GUIStrategyType type, int count) {
        Player player = type == GUIStrategyType.Row ? game.getPlayer1() : game.getPlayer2();
        if (count < player.getStrategies().size()) {
            while (count != player.getStrategies().size()) {
                removeStrategy(type);
            }
        } else if (count > player.getStrategies().size()) {
            while (count != player.getStrategies().size()) {
                addNormalStrategy(type, null);
            }
        }
    }

    private void addStrategy(GUIStrategyType type, String name, boolean optimalStrategy) {
        int totalCount = type == GUIStrategyType.Row ? getRowCount() : getColumnCount();
        int lastStrategyIndex = totalCount - 1;
        int strategyCount = totalCount - 2;
        Player player = type == GUIStrategyType.Row ? game.getPlayer1() : game.getPlayer2();
        String stratName;
        if (name == null) {
            stratName = "Strategie" + strategyCount;
            while (game.getStrategy(stratName, player) != null) {
                strategyCount++;
                stratName = "Strategie" + strategyCount;
            }
        } else {
            stratName = name;
        }
        if (game.getStrategy(stratName, player) == null && !optimalStrategy) {
            game.addStrategy(stratName, player);
        }
        TextField tf = createStrategyTextField(stratName, optimalStrategy);
        if (type == GUIStrategyType.Row) {
            addStrategyFieldRow(tf, lastStrategyIndex, strategyCount);
        } else {
            addStrategyFieldColumn(tf, lastStrategyIndex, strategyCount);
        }
        setupContextMenus();
    }

    private void addNormalStrategy(GUIStrategyType type, String name) {
        int totalCount = type == GUIStrategyType.Row ? getRowCount() : getColumnCount();
        int lastStrategyIndex = totalCount - 1;
        addStrategy(type, name, false);
        addNewNormalStrategyVectors(type, lastStrategyIndex);
    }

    private void addOptimalStrategy(GUIStrategyType type, String name, MixedNashEquilibrium mne) {
        int totalCount = type == GUIStrategyType.Row ? getRowCount() : getColumnCount();
        int lastStrategyIndex = totalCount - 1;
        addStrategy(type, name, true);
        addNewOptimalStrategyVectors(type, lastStrategyIndex, mne);
    }

    private void addStrategyFieldRow(TextField tf, int lastStrategyIndex, int strategyCount) {
        grid.getRowConstraints().get(lastStrategyIndex).setMinHeight(40);
        grid.getRowConstraints().get(lastStrategyIndex).setPrefHeight(40);
        grid.getRowConstraints().get(lastStrategyIndex).setMaxHeight(40);
        RowConstraints rc = new RowConstraints();
        rc.setPrefHeight(1080);
        grid.getRowConstraints().add(rc);
        Node firstPlayer = getNodeByRowColumnIndex(2, 0);
        GridPane.setRowSpan(firstPlayer, strategyCount);
        grid.add(tf, 1, lastStrategyIndex);
        GridPane.setRowIndex(list, GridPane.getRowIndex(list) + 1);

        scrollPane.setPrefHeight(scrollPane.getPrefHeight() + 40);
    }

    private void addStrategyFieldColumn(TextField tf, int lastStrategyIndex, int strategyCount) {
        tf.setAlignment(Pos.CENTER);
        grid.getColumnConstraints().get(lastStrategyIndex).setMinWidth(90);
        grid.getColumnConstraints().get(lastStrategyIndex).setPrefWidth(90);
        grid.getColumnConstraints().get(lastStrategyIndex).setMaxWidth(90);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPrefWidth(1920);
        grid.getColumnConstraints().add(cc);
        Node secondPlayer = getNodeByRowColumnIndex(0, 2);
        GridPane.setColumnSpan(secondPlayer, strategyCount);
        grid.add(tf, lastStrategyIndex, 1);

        GridPane.setColumnSpan(list, GridPane.getColumnSpan(list) + 1);

        scrollPane.setPrefWidth(scrollPane.getPrefWidth() + 40);
    }

    private void addNewNormalStrategyVectors(GUIStrategyType type, int strategyIndex) {
        int otherStrategyCount = type == GUIStrategyType.Row ? getColumnCount() - 1 : getRowCount() - 1;
        for (int i = 2; i < otherStrategyCount; i++) {
            VectorField vf = createVectorField(false);
            if (type == GUIStrategyType.Row)
                grid.add(vf, i, strategyIndex);
            else
                grid.add(vf, strategyIndex, i);
        }
    }

    private void addNewOptimalStrategyVectors(GUIStrategyType type, int strategyIndex, MixedNashEquilibrium mne) {
        Player player = type == GUIStrategyType.Row ? game.getPlayer1() : game.getPlayer2();
        int otherStrategyCount = type == GUIStrategyType.Row ? getColumnCount() - 1 : getRowCount() - 1;
        for (int i = 2; i < otherStrategyCount; i++) {
            VectorField vf = createVectorField(true);
            int stratIndex = i - 2;
            Player otherPlayer = player == game.getPlayer1() ? game.getPlayer2() : game.getPlayer1();
            try {
                String strategy = otherPlayer.getStrategies().get(stratIndex).getName();
                vf.setText(game.getMixedPayoff(mne, strategy, otherPlayer).toString());
            } catch (IndexOutOfBoundsException ioobe) {
                vf.setText(game.getOptimalMixedPayoff(mne).toString());
            }

            if (type == GUIStrategyType.Row)
                grid.add(vf, i, strategyIndex);
            else
                grid.add(vf, strategyIndex, i);
        }
    }

    private void removeStrategy(Strategy strategy) {
        if (menubar != null) {
            menubar.requestFocus();
        }
        Player player = game.getPlayer1().getStrategies().contains(strategy) ? game.getPlayer1() : game.getPlayer2();
        if (player.getStrategies().size() <= 1) {
            return;
        }
        GUIStrategyType type = player.equals(game.getPlayer1()) ? GUIStrategyType.Row : GUIStrategyType.Column;
        removeGUIStrategy(type);
        game.removeStrategy(strategy.getName(), player);
        refreshGUI();
    }

    private void removeStrategy(GUIStrategyType type) {
        if (menubar != null) {
            menubar.requestFocus();
        }
        Player player = type == GUIStrategyType.Row ? game.getPlayer1() : game.getPlayer2();
        Strategy strategy = player.getStrategies().get(player.getStrategies().size() - 1);
        game.removeStrategy(strategy.getName(), player);

        removeGUIStrategy(type);
    }

    private void removeGUIStrategy(GUIStrategyType type) {
        int totalCount = type == GUIStrategyType.Row ? getRowCount() : getColumnCount();
        int strategyCount = totalCount - 2;
        int otherStrategyCount = type == GUIStrategyType.Row ? getColumnCount() - 1 : getRowCount() - 1;
        for (int i = 1; i < otherStrategyCount; i++) {
            if (type == GUIStrategyType.Row) {
                grid.getChildren().remove(getNodeByRowColumnIndex(strategyCount, i));
            } else {
                grid.getChildren().remove(getNodeByRowColumnIndex(i, strategyCount));
            }
        }
        if (type == GUIStrategyType.Row) {
            Node firstPlayer = getNodeByRowColumnIndex(2, 0);
            GridPane.setRowSpan(firstPlayer, GridPane.getRowSpan(firstPlayer) - 1);
            grid.getRowConstraints().remove(grid.getRowConstraints().get(strategyCount));
            grid.getRowConstraints().get(getRowCount() - 1).setMinHeight(40);
            grid.getRowConstraints().get(getRowCount() - 1).setPrefHeight(1080);
            grid.getRowConstraints().get(getRowCount() - 1).setMaxHeight(1080);
            if (list != null && grid.getChildren().contains(list)) {
                GridPane.setRowIndex(list, GridPane.getRowIndex(list) - 1);
            }
            scrollPane.setPrefHeight(scrollPane.getPrefHeight() - 40);
        } else {
            scrollPane.setPrefWidth(scrollPane.getPrefWidth() - 40);
            Node secondPlayer = getNodeByRowColumnIndex(0, 2);
            GridPane.setColumnSpan(secondPlayer, GridPane.getColumnSpan(secondPlayer) - 1);
            grid.getColumnConstraints().remove(grid.getColumnConstraints().get(strategyCount));
            grid.getColumnConstraints().get(getColumnCount() - 1).setMinWidth(0);
            grid.getColumnConstraints().get(getColumnCount() - 1).setPrefWidth(1920);
            grid.getColumnConstraints().get(getColumnCount() - 1).setMaxWidth(1920);
        }
    }

    private void openEditScene() {
        if (propertiesOpened) {
            editStage.requestFocus();
            return;
        }
        try {
            initEditStage();
            editStage.show();
            propertiesOpened = true;
        } catch (IOException ex) {
            Logger.getLogger(NormalGameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initEditStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Edit.fxml"));
        loader.setController(new EditController(game, this, editStage));
        Scene scene = new Scene(loader.load());
        editStage = new Stage();
        editStage.initStyle(StageStyle.UTILITY);
        editStage.initOwner(menubar.getScene().getWindow());
        editStage.setScene(scene);
        editStage.setOnCloseRequest(e -> propertiesOpened = false);
    }

    private void textFieldFocusChanged(TextField node, ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean focused) {
        String text = node.getText();
        if (focused) {
            currentFocusedText = text;
        }
        if (!focused) {
            if (isFirstPlayerField(currentFocusedText)) {
                game.getPlayer1().setName(text);
            } else if (isSecondPlayerField(currentFocusedText)) {
                game.getPlayer2().setName(text);
            } else if (isFirstPlayerStrategyField(GridPane.getColumnIndex(node))) {
                try {
                    game.renameStrategy(currentFocusedText, text, game.getPlayer1());
                } catch (IllegalArgumentException e) {
                    node.setText(currentFocusedText);
                }
            } else {
                try {
                    game.renameStrategy(currentFocusedText, text, game.getPlayer2());
                } catch (IllegalArgumentException e) {
                    node.setText(currentFocusedText);
                }
            }
        }
    }

    private void vectorFieldFocusChanged(VectorField node, ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean focused) {
        if (!focused && node.isEditable()) {
            int row = GridPane.getRowIndex(node);
            int column = GridPane.getColumnIndex(node);
            String firstStrategy;
            String secondStrategy;
            try {
                firstStrategy = ((TextField) getNodeByRowColumnIndex(row, 1)).getText();
                secondStrategy = ((TextField) getNodeByRowColumnIndex(1, column)).getText();
            } catch (NullPointerException e) {
                return;
            }
            StrategyPair pair = new StrategyPair(game.getStrategy(firstStrategy, game.getPlayer1()), game.getStrategy(secondStrategy, game.getPlayer2()));
            float[] values = node.getValues();
            game.setVector(pair, new Vector(values[0], values[1]));
        }
    }

    private boolean isFirstPlayerField(String content) {
        return game.getPlayer1().getName().equals(content);
    }

    private boolean isSecondPlayerField(String content) {
        return game.getPlayer2().getName().equals(content);
    }

    private boolean isFirstPlayerStrategyField(int column) {
        return column == 1;
    }

    private void setListViewContent(List<? extends NashEquilibrium> content) {
        list.getSelectionModel().clearSelection();
        list.setItems(FXCollections.observableArrayList(content));
    }

    private Node getStrategyField(Strategy strategy, Player player) {
        int index = player.getStrategies().indexOf(strategy);
        int guiIndex = index + 2;

        if (player.equals(game.getPlayer1())) {
            return getNodeByRowColumnIndex(guiIndex, 1);
        } else {
            return getNodeByRowColumnIndex(1, guiIndex);
        }
    }

    private Node getField(Strategy strat1, Strategy strat2) {
        int player1Index = game.getPlayer1().getStrategies().indexOf(strat1);
        int rowIndex = player1Index + 2;

        int player2Index = game.getPlayer2().getStrategies().indexOf(strat2);
        int columnIndex = player2Index + 2;

        return getNodeByRowColumnIndex(rowIndex, columnIndex);
    }

    private TextField createStrategyTextField(String stratName, boolean optimalStrategy) {
        TextField tf = new TextField();
        tf.setPrefHeight(40);
        tf.setText(stratName);
        if (optimalStrategy) {
            tf.setEditable(false);
            tf.setOpacity(.5f);
        }
        tf.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            textFieldFocusChanged(tf, arg0, oldPropertyValue, newPropertyValue);
        });
        tf.setOnAction(e -> {
            grid.requestFocus();
        });
        return tf;
    }

    private VectorField createVectorField(boolean optimalField) {
        VectorField vf = new VectorField();
        if (optimalField) {
            vf.setEditable(false);
            vf.setOpacity(.5f);
        }
        vf.setPrefHeight(40);
        vf.setText("0;0");
        vf.setAlignment(Pos.CENTER);
        vf.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) -> {
            vectorFieldFocusChanged(vf, arg0, oldPropertyValue, newPropertyValue);
        });
        vf.setOnAction(e -> {
            grid.requestFocus();
        });
        return vf;
    }

    private Node getNodeByRowColumnIndex(final int row, final int column) {
        ObservableList<Node> children = grid.getChildren();
        for (Node node : children) {
            int rIndex = GridPane.getRowIndex(node) == null ? 0 : GridPane.getRowIndex(node);
            int cIndex = GridPane.getColumnIndex(node) == null ? 0 : GridPane.getColumnIndex(node);
            if (rIndex == row && cIndex == column) {
                return node;
            }
        }
        return null;
    }

    private int getRowCount() {
        return grid.getRowConstraints().size();
    }

    private int getColumnCount() {
        return grid.getColumnConstraints().size();
    }
}
