import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Collections;
import java.io.FileInputStream;



public class PuzzleGame extends Application {

    private final int SIZE = 3; // 3x3
    private final double TILE_SIZE = 150;
    private List<ImageView> tiles = new ArrayList<>();
    private ImageView blankTile;
    private GridPane grid;

    @Override
        public void start(Stage stage) throws Exception {
        Image image = new Image(new FileInputStream("anime.jpg")); 


            grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(4);
            grid.setVgap(4);

       // Split image into 9 tiles
        double pieceWidth = image.getWidth() / SIZE;
        double pieceHeight = image.getHeight() / SIZE;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                ImageView iv = new ImageView(image);
                iv.setViewport(new javafx.geometry.Rectangle2D(
                        j * pieceWidth, i * pieceHeight, pieceWidth, pieceHeight
                ));
                iv.setFitWidth(TILE_SIZE);
                iv.setFitHeight(TILE_SIZE);
                tiles.add(iv);
            }
        }


            // Make last tile blank
            blankTile = new ImageView();
            blankTile.setFitWidth(TILE_SIZE);
            blankTile.setFitHeight(TILE_SIZE);
            blankTile.setStyle("-fx-background-color: white; -fx-border-color: #999;");
            tiles.set(tiles.size() - 1, blankTile);

            // Shuffle tiles
            Collections.shuffle(tiles);

            refreshGrid();

            for (ImageView iv : tiles) {
                iv.setOnMouseClicked(e -> moveTile(iv));
            }

            Button shuffleBtn = new Button("🔀 Shuffle");
            shuffleBtn.setOnAction(e -> {
                Collections.shuffle(tiles);
                refreshGrid();
            });

            VBox root = new VBox(20, grid, shuffleBtn);
            root.setAlignment(Pos.CENTER);
            root.setId("root");

            Scene scene = new Scene(root, 550, 650);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            stage.setTitle("🎨 JavaFX Image Puzzle");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        int bi = tiles.indexOf(blankTile);

        if (isAdjacent(ci, bi)) {
            Collections.swap(tiles, ci, bi);
            refreshGrid();
            if (isSolved()) showWinAlert();

        }
    }



    private boolean isAdjacent(int a, int b) {
        return (a == b - 1 && b % SIZE != 0)
                || (a == b + 1 && a % SIZE != 0)
                || (a == b - SIZE)
                || (a == b + SIZE);
    }

    private boolean isSolved() {
        for (int i = 0; i < tiles.size(); i++) {
            if (!tiles.get(i).getUserData().equals(correctOrder.get(i).getUserData())) {
                return false;
            }
        }



    private void showWinAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Congratulations!");
        alert.setHeaderText(null);
        alert.setContentText("You solved the puzzle!");
        alert.showAndWait();


    public static void main(String[] args) {
        launch();
    }
}



