import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleGame extends Application {

    private static final int SIZE = 3;
    private static final int TILE_SIZE = 150;
    private List<ImageView> tiles = new ArrayList<>();
    private GridPane grid;
    private ImageView blankTile; // Add reference for blank tile

    @Override
    public void start(Stage stage) throws Exception {
        Image image = new Image(new FileInputStream("anime.jpg"));

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(0);
        grid.setVgap(0);
        grid.getStyleClass().add("grid-pane");

        double pieceWidth = image.getWidth() / SIZE;
        double pieceHeight = image.getHeight() / SIZE;

        int id = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                ImageView iv = new ImageView(image);
                iv.setViewport(new Rectangle2D(
                        j * pieceWidth, i * pieceHeight, pieceWidth, pieceHeight
                ));
                iv.setFitWidth(TILE_SIZE);
                iv.setFitHeight(TILE_SIZE);
                iv.setUserData(id); // store correct index
                tiles.add(iv);
                id++;
            }
        }

        // Last tile = blank
        blankTile = new ImageView();
        blankTile.setFitWidth(TILE_SIZE);
        blankTile.setFitHeight(TILE_SIZE);
        blankTile.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        blankTile.setUserData(SIZE * SIZE - 1); // mark as blank
        tiles.set(tiles.size() - 1, blankTile);

        Collections.shuffle(tiles);
        refreshGrid();

        Button shuffleBtn = new Button("🔀 Shuffle");
        shuffleBtn.setOnAction(e -> {
            Collections.shuffle(tiles);
            refreshGrid();
        });

        VBox root = new VBox(20, grid, shuffleBtn);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, TILE_SIZE * SIZE + 100, TILE_SIZE * SIZE + 100);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Optional CSS

        stage.setScene(scene);
        stage.setTitle("🧩 Cartoon Image Puzzle");
        stage.show();
    }

    private void refreshGrid() {
        grid.getChildren().clear();
        for (int i = 0; i < SIZE * SIZE; i++) {
            int row = i / SIZE;
            int col = i % SIZE;
            ImageView tile = tiles.get(i);
            tile.setOnMouseClicked(e -> moveTile(tile));
            grid.add(tile, col, row);
        }
    }

    private void moveTile(ImageView clicked) {
        int ci = tiles.indexOf(clicked);
        int bi = findBlankIndex();

        if (isAdjacent(ci, bi)) {
            Collections.swap(tiles, ci, bi);
            refreshGrid();
            if (isSolved()) showWinAlert();
        }
    }

    private int findBlankIndex() {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) == blankTile) return i;
        }
        return -1;
    }

    private boolean isAdjacent(int a, int b) {
        return (a == b - 1 && b % SIZE != 0) ||
               (a == b + 1 && a % SIZE != 0) ||
               (a == b - SIZE) || (a == b + SIZE);
    }

    // ONLY CHANGE: Correct win-check logic
private boolean isSolved() {
    for (int i = 0; i < tiles.size(); i++) {
        ImageView tile = tiles.get(i);
        if (tile == blankTile) continue; // skip blank tile
        if ((int) tile.getUserData() != i) return false; // check if tile is in correct position
    }
    return true;
}



    private void showWinAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Congratulations!");
        alert.setHeaderText(null);
        alert.setContentText("You solved the puzzle!");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
