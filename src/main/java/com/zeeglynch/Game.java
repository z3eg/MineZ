package com.zeeglynch;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by dmytrocherednyk on 11.11.15.
 */
public class Game extends Application {

    public byte horizontalCellCount = 25;
    public byte verticalCellCount = 20;
    public int bombCount = 100;
    //    public int bombsLeft;
    public static int revealedCellsCount = 0;
    public int freeCellsCount;
    public boolean gameLost = false;
    public boolean gameWon = false;
    public double cellWidth = 30;
    public double cellHeight = 30;
    public Stage stage;
    public Cell cells[][] = new Cell[horizontalCellCount][verticalCellCount];
    Label statusBar;
    Label infoBar;

    GridPane pane;
    EventHandler handler = new EventHandler() {
        public void handle(Event event) {
            if (gameLost) {
                restart();
            } else {
                Cell curCell;
                for (int i = 0; i < cells.length; i++) {
                    for (int j = 0; j < cells[i].length; j++) {
                        if (event.getSource() == cells[i][j]) {

                            curCell = cells[i][j];
                            if (revealedCellsCount == 0) {
                                while (curCell.getValue() > 8) {
                                    cells = plantBombs(cells, bombCount);
                                }
                            }
//                            revealedCellsCount++;
                            renderInfoBar();
                            if (revealedCellsCount == freeCellsCount) {
                                statusBar.setText("GAME WON!!");
                                revealTheField(cells);
                                gameWon = true;
                            }
                            revealTheSector(curCell);
                            if (curCell.getValue() > 8) {
                                statusBar.setText("GAME OVER.");
                                revealTheField(cells);
                                gameLost = true;
                                    /*for (int k = 0; k < cells.length; k++) {
                                        for (int l = 0; l < cells[k].length; l++) {
                                            cells[k][l].setDisable(false);
                                        }
                                    }*/
                                cells[0][0].setDisable(false);
                            }
                        }
                    }
                }
            }
        }
    };

    private class Cell extends Button {
        int value = 0;
        int x;
        int y;
        boolean isRevealed = false;

        public Cell(int value, int x, int y) {
            this.value = value;
            this.x = x;
            this.y = y;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static Color getContrastColor(Color color) {
        return color.invert();
    }

    public static void revealTheCell(Cell cell) {
        if (!cell.isRevealed) {
            Color[] colors = {Color.LIME, Color.GREEN, Color.YELLOWGREEN, Color.GOLDENROD, Color.ORANGERED, Color.MAROON, Color.CRIMSON, Color.DEEPPINK, Color.MAGENTA, Color.BLACK};
            int cellValue = cell.getValue();
            Color color = cellValue < 9 ? colors[cellValue] : colors[colors.length - 1];
            Insets outsets = cell.getBackground().getOutsets();
            cell.setTextFill(getContrastColor(color));
            cell.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, outsets)));
            String cellText = cellValue == 0 ? " " : (cellValue < 9 ? Integer.toString(cellValue) : "x");
            cell.setText(cellText);
            cell.setDisable(true);
            revealedCellsCount++;
            System.out.println("Cell revealed~");
            cell.isRevealed = true;
        }
    }

    public void revealTheField(Cell[][] cells) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                revealTheCell(cells[i][j]);
            }

        }
    }

    public void renderStatusBar() {
        statusBar.setText("");
    }

    public void renderInfoBar() {
        infoBar.setText("Bomb amount: " + bombCount + "\t Free cells: " + freeCellsCount + "\n Revealed cells: " + revealedCellsCount);
    }

    public void printTheField(Cell[][] testField) {
        for (int i = 0; i < testField.length; i++) {
            for (int j = 0; j < testField[i].length; j++) {
                System.out.print((testField[i][j].getValue() < 9 ? testField[i][j].getValue() : "x") + " ");
            }
            System.out.println();
        }
    }

    public Cell[][] plantBombs(Cell[][] cells, int bombCount) {
        freeCellsCount = cells.length * cells[0].length - bombCount;
        Cell[][] newCells = cells.clone();
        Random rand = new Random();
        int bombXPos;
        int bombYPos;
        for (int i = 0; i < newCells.length; i++) {
            for (int j = 0; j < newCells[i].length; j++) {
                newCells[i][j].setValue(0);
            }
        }
        for (int b = 0; b < bombCount; b++) {
            do {
                bombXPos = rand.nextInt(cells.length);
                bombYPos = rand.nextInt(cells[0].length);
            }
            while (cells[bombXPos][bombYPos].getValue() > 8);

            for (int i = bombXPos - 1; i <= bombXPos + 1; i++) {
                for (int j = bombYPos - 1; j <= bombYPos + 1; j++) {
                    if (i > -1 && j > -1 && i < cells.length && j < cells[0].length) {
                        newCells[i][j].setValue(newCells[i][j].getValue() + 1);
                    }
                }
            }
            cells[bombXPos][bombYPos].setValue(9);
        }
        return newCells;
    }

    public void prepare(Stage primaryStage) {
        primaryStage.setTitle("Glomines");
        Cell tmpCell;
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
        pane = new GridPane();

        for (int i = 0; i < cells.length; i++) {
            pane.getChildren().addAll(new ArrayList(Arrays.asList(cells[i])));
        }
        statusBar = new Label("Press any cell to start");
        infoBar = new Label("Bomb amount: " + bombCount + "\t Free cells: " + freeCellsCount + "\n Revealed cells: " + revealedCellsCount);
        GridPane.setConstraints(statusBar, 0, cells.length, cells[0].length, 1);
        GridPane.setConstraints(infoBar, 0, cells.length+1, cells[0].length, 1);
        pane.getChildren().add(statusBar);
        pane.getChildren().add(infoBar);

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public void restart() {
        Cell tmpCell;
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

    public void revealTheSector(int cellX, int cellY) {
        Cell curCell = cells[cellX][cellY];
        if (curCell.getValue() == 0) {
            revealSurroundings(cellX, cellY);
        } else if (curCell.getValue() < 9) {
            System.out.println("Revelation is dangerous");
            revealTheCell(curCell);
        }
    }

    public void revealTheSector(Cell curCell) {
        int cellX = curCell.x;
        int cellY = curCell.y;
        if (curCell.getValue() == 0) {
            ArrayList<Cell> revealedCells = revealSurroundings(cellX, cellY);
            for (Cell revealedCell : revealedCells) {
                if (revealedCell.getValue() == 0) {

                    revealSurroundings(revealedCell);
                }
            }
        } else if (curCell.getValue() < 9) {
            System.out.println("Revelation is dangerous");
            revealTheCell(curCell);
        }
    }

    public ArrayList<Cell> revealSurroundings(int cellX, int cellY) {
        ArrayList<Cell> cellsRevealed = new ArrayList<Cell>();
        for (int i = cellX - 1; i <= cellX + 1; i++) {
            for (int j = cellY - 1; j <= cellY + 1; j++) {
                if (i > -1 && j > -1 && i < this.cells.length && j < this.cells[0].length) {
                    revealTheCell(this.cells[i][j]);
                    cellsRevealed.add(cells[i][j]);
//                    revealSurroundings(cells[i][j]);
                }
            }
        }
        return cellsRevealed;
    }

    public ArrayList<Cell> revealSurroundings(Cell cell) {
        return revealSurroundings(cell.x, cell.y);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        prepare(primaryStage);

    }
}
