package com.zeeglynch;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by dmytrocherednyk on 11.11.15.
 */
public class Game extends Application {

    private byte columnCount = 15;
    private byte rowCount = 15;
    private int bombCount = 20;
    private int bombsMarked;
    private static int revealedCellsCount = 0;
    private int freeCellsCount;
    private boolean gameLost = false;
    private boolean gameWon = false;
    private double cellWidth = 30;
    private double cellHeight = 30;
    private Stage stage;
    private Cell cells[][];
    private Label statusBar;
    private Label infoBar;
    private long timePlayed = 0;
    private String gameState = "Set game ";
    private long timeStarted;
    private long now;
    GridPane pane;


    EventHandler handler = new EventHandler<MouseEvent>() {


        @Override
        public void handle(MouseEvent event) {
            if (gameLost) {
                showSettingsPanel();
            } else if (gameWon) {
                System.out.println("GAME WON!");
                restart();
            } else {
                Cell curCell;
                for (int i = 0; i < cells.length; i++) {
                    for (int j = 0; j < cells[i].length; j++) {

                        if (event.getSource() == cells[i][j]) {
                            if (event.getButton() == MouseButton.PRIMARY) {

                                if (!cells[i][j].isMarked) {
                                    curCell = cells[i][j];
                                    if (revealedCellsCount == 0) {
                                        while (curCell.value > 8) {
                                            cells = plantBombs(cells, bombCount);
                                        }
                                        timeStarted = System.currentTimeMillis();
                                    }
                                    renderInfoBar();
                                    renderStatusBar();
                                    if (revealedCellsCount == freeCellsCount - 1) {
                                        gameState = "YOU WON!";
                                        statusBar.setText(gameState);
                                        revealTheField(cells);
                                        gameWon = true;
                                        cells[0][0].setDisable(false);
                                        showSettingsPanel();
                                    }
                                    revealTheSector(curCell);
                                    if (curCell.value > 8) {
                                        gameState = "YOU LOST";
                                        statusBar.setText(gameState);
                                        revealTheField(cells);
                                        gameLost = true;
                                        cells[0][0].setDisable(false);
                                        showSettingsPanel();
                                    }
                                }
                            } else if (event.getButton() == MouseButton.SECONDARY) {
                                cells[i][j].flag();
                            }
                        }
                    }
                }
            }
        }
    };

    private class Cell extends Button {
        private int value = 0;
        private int x;
        private int y;
        private boolean isRevealed = false;
        private boolean isMarked = false;

        private Cell(int value, int x, int y) {
            this.value = value;
            this.x = x;
            this.y = y;
        }

        private void flag() {
            if (isMarked) {
                unmark();
            } else {
                mark();
            }
        }

        private void mark() {
            if (!isRevealed && !isMarked) {
                setText("B");
                isMarked = true;
                bombsMarked++;
//                setDisable(true);
            }
        }

        private void unmark() {
            if (!isRevealed && isMarked) {
                setText("");
                isMarked = false;
                bombsMarked--;
//                setDisable(false);
            }
        }

        private void reveal() {
            if (!isRevealed) {
                Color[] colors = {Color.LIME, Color.GREEN, Color.YELLOWGREEN, Color.GOLDENROD, Color.ORANGERED,
                        Color.MAROON, Color.CRIMSON, Color.DEEPPINK, Color.MAGENTA, Color.BLACK};
                Color color = value < 9 ? colors[value] : colors[colors.length - 1];
                Insets outsets = getBackground().getOutsets();
                setTextFill(getContrastColor(color));
                setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, outsets)));
                String cellText = value == 0 ? " " : (value < 9 ? Integer.toString(value) : "x");
                setText(cellText);
                setDisable(true);
                revealedCellsCount++;
                System.out.println("REVEALED CELLS COUNT: " + revealedCellsCount);
//                System.out.println("Cell revealed~");
                if (isMarked) {
                    unmark();

                }
                isRevealed = true;


            }
        }

    }

    private void showSettingsPanel() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        final VBox dialogVbox = new VBox();

        dialogVbox.getChildren().add(new Text(gameState));
        dialogVbox.getChildren().add(new Text("Row count:"));
        final TextField rowCountField = new TextField(Byte.toString(rowCount));
        dialogVbox.getChildren().add(rowCountField);
        dialogVbox.getChildren().add(new Text("Column count:"));
        final TextField columnCountField = new TextField(Byte.toString(columnCount));
        dialogVbox.getChildren().add(columnCountField);
        dialogVbox.getChildren().add(new Text("Bomb count:"));
        final TextField bombCountField = new TextField(Integer.toString(bombCount));
        dialogVbox.getChildren().add(bombCountField);
        Button startButton = new Button("Start!");
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    rowCount = Byte.parseByte(rowCountField.getText());
                    columnCount = Byte.parseByte(columnCountField.getText());
                    bombCount = Integer.parseInt(bombCountField.getText());
                    if (bombCount > rowCount * columnCount - 2) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Please, set larger field resolution or lesser bomb amount.");
                        alert.show();
                    } else if (bombCount < 1 || rowCount < 1 || columnCount < 1) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Numbers should be positive, ASSHOLE.");
                        alert.show();
                    } else {
                        restart();
                        dialog.close();
                    }
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please, use NUMBERS to set dimensions and bomb amount.");
                    alert.show();
                }
            }
        });
        dialogVbox.getChildren().add(startButton);
        dialogVbox.setSpacing(10);
        Scene dialogScene = new Scene(dialogVbox);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private static Color getContrastColor(Color color) {
        return color.invert();
    }

    private void revealTheField(Cell[][] cells) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j].reveal();
            }

        }
    }

    private String formatTime(long timeInMillis) {
        long seconds = timeInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        byte minutesToPrint = (byte) (minutes % 60);
        byte secondsToPrint = (byte) (seconds % 60);
        return (hours + ":" + minutesToPrint + ":" + secondsToPrint);
    }

    private void renderStatusBar() {
        if (gameWon) {
            statusBar.setText("GAME WON! CONGRATS!");
        } else if (gameLost) {
            statusBar.setText("GAME LOST! TRY AGAIN!");
        } else if (revealedCellsCount == 0) {
            statusBar.setText("Press any cell to start.");
        } else {
            now = System.currentTimeMillis();
            timePlayed = now - timeStarted;
            statusBar.setText("Time played: " + formatTime(timePlayed) + ".");
        }
    }

    private void renderInfoBar() {
        infoBar.setText("Bomb amount: " + bombCount + "\t Bombs marked: " + bombsMarked
                + "\n Bombs left: " + (bombCount - bombsMarked) + "\t Free cells: " + freeCellsCount
                + "\t Revealed cells: " + revealedCellsCount);
    }

    private void printTheField(Cell[][] testField) {
        for (int i = 0; i < testField.length; i++) {
            for (int j = 0; j < testField[i].length; j++) {
                System.out.print((testField[i][j].value < 9 ? testField[i][j].value : "x") + " ");
            }
            System.out.println();
        }
    }

    private Cell[][] plantBombs(Cell[][] cells, int bombCount) {
        freeCellsCount = cells.length * cells[0].length - bombCount;
        Cell[][] newCells = cells.clone();
        Random rand = new Random();
        int bombXPos;
        int bombYPos;
        for (int i = 0; i < newCells.length; i++) {
            for (int j = 0; j < newCells[i].length; j++) {
                newCells[i][j].value = 0;
            }
        }
        for (int b = 0; b < bombCount; b++) {
            do {
                bombXPos = rand.nextInt(cells.length);
                bombYPos = rand.nextInt(cells[0].length);
            }
            while (cells[bombXPos][bombYPos].value > 8);

            for (int i = bombXPos - 1; i <= bombXPos + 1; i++) {
                for (int j = bombYPos - 1; j <= bombYPos + 1; j++) {
                    if (i > -1 && j > -1 && i < cells.length && j < cells[0].length) {
                        newCells[i][j].value = (newCells[i][j].value + 1);
                    }
                }
            }
            cells[bombXPos][bombYPos].value = 9;

        }
        bombsMarked = 0;
        return newCells;
    }

    private void prepare(Stage primaryStage) {
        primaryStage.setTitle("MineZ");
        Cell tmpCell;
        cells = new Cell[columnCount][rowCount];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                tmpCell = new Cell(0, i, j);
                tmpCell.setMinSize(cellWidth, cellHeight);
                tmpCell.setMaxSize(cellWidth, cellHeight);
                cells[i][j] = tmpCell;
//                cells[i][j].setOnAction(newHandler);
                cells[i][j].setOnMouseClicked(handler);
                GridPane.setConstraints(cells[i][j], i, j);
            }
        }
        cells = plantBombs(cells, bombCount);
        pane = new GridPane();

        for (int i = 0; i < cells.length; i++) {
            pane.getChildren().addAll(new ArrayList(Arrays.asList(cells[i])));
        }
        statusBar = new Label("Press any cell to start");
        infoBar = new Label("Bomb amount: " + bombCount + "\t Free cells: " + freeCellsCount + "\n Revealed cells: " + revealedCellsCount);
        GridPane.setConstraints(statusBar, 0, cells.length, cells[0].length, 1);
        GridPane.setConstraints(infoBar, 0, cells.length + 1, cells[0].length, 1);
        pane.getChildren().add(statusBar);
        pane.getChildren().add(infoBar);
        Button restartButton = new Button("Restart/Change dimensions");
        restartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSettingsPanel();

            }
        });
        GridPane.setConstraints(restartButton, 0, cells.length + 2, cells[0].length, 1);
        pane.getChildren().add(restartButton);

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void restart() {
        Cell tmpCell;
        cells = new Cell[columnCount][rowCount];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                tmpCell = new Cell(0, i, j);
                tmpCell.setMinSize(cellWidth, cellHeight);
                tmpCell.setMaxSize(cellWidth, cellHeight);
                cells[i][j] = tmpCell;
                cells[i][j].setOnAction(handler);
                GridPane.setConstraints(cells[i][j], i, j);
            }
        }
        cells = plantBombs(cells, bombCount);
        revealedCellsCount = 0;
        gameLost = false;
        prepare(stage);

    }

    private void revealTheSector(int cellX, int cellY) {
        Cell curCell = cells[cellX][cellY];
        if (curCell.value == 0) {
            revealSurroundings(cellX, cellY);
        } else if (curCell.value < 9) {
            System.out.println("Revelation is dangerous");
            curCell.reveal();
        }
    }

    private void revealTheSector(Cell curCell) {
        int cellX = curCell.x;
        int cellY = curCell.y;
        if (curCell.value == 0) {
            ArrayList<Cell> revealedCells = revealSurroundings(cellX, cellY);
            for (Cell revealedCell : revealedCells) {
                if (revealedCell.value == 0) {

                    revealSurroundings(revealedCell);
                }
            }
        } else if (curCell.value < 9) {
            System.out.println("Revelation is dangerous");
            curCell.reveal();
        }
    }

    private ArrayList<Cell> revealSurroundings(int cellX, int cellY) {
        ArrayList<Cell> cellsRevealed = new ArrayList<Cell>();
        for (int i = cellX - 1; i <= cellX + 1; i++) {
            for (int j = cellY - 1; j <= cellY + 1; j++) {
                if (i > -1 && j > -1 && i < this.cells.length && j < this.cells[0].length) {
                    cells[i][j].reveal();
                    cellsRevealed.add(cells[i][j]);
//                    revealSurroundings(cells[i][j]);
                }
            }
        }
        return cellsRevealed;
    }

    private ArrayList<Cell> revealSurroundings(Cell cell) {
        return revealSurroundings(cell.x, cell.y);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        prepare(primaryStage);

    }
}
