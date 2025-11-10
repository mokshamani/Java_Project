import javafx.animation.TranslateTransition;
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

public class PuzzleGame extends Application {

    private final int SIZE = 3; // 3x3
    private final double TILE_SIZE = 150;
    private List<ImageView> tiles = new ArrayList<>();
    private ImageView blankTile;
    private GridPane grid;

    @Override
    public void start(Stage stage) {
        try {
            URL url = new URL("https://static.vecteezy.com/system/resources/previews/028/794/706/non_2x/cartoon-cute-school-boy-photo.jpg");
            Image image = new Image(url.openStream(), TILE_SIZE * SIZE, TILE_SIZE * SIZE, false, true);

            grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(4);
            grid.setVgap(4);

            // Create puzzle tiles
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    ImageView iv = new ImageView(image);
                    iv.setFitWidth(TILE_SIZE);
                    iv.setFitHeight(TILE_SIZE);
                    iv.setViewport(new javafx.geometry.Rectangle2D(
                            col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                    iv.setUserData(row * SIZE + col);
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

            updateGrid();

            for (ImageView iv : tiles) {
                iv.setOnMouseClicked(e -> moveTile(iv));
            }

            Button shuffleBtn = new Button("🔀 Shuffle");
            shuffleBtn.setOnAction(e -> {
                Collections.shuffle(tiles);
                updateGrid();
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

    private void updateGrid() {
        grid.getChildren().clear();
        for (int i = 0; i < tiles.size(); i++) {
            int row = i / SIZE;
            int col = i % SIZE;
            grid.add(tiles.get(i), col, row);
        }
    }

    private void moveTile(ImageView clicked) {
        if (clicked == blankTile) return;

        int ci = tiles.indexOf(clicked);
        int bi = tiles.indexOf(blankTile);

        if (isAdjacent(ci, bi)) {
            Collections.swap(tiles, ci, bi);
            animateMove(clicked, ci, bi);
            updateGrid();
            checkWin();
        }
    }

    private void animateMove(ImageView tile, int fromIndex, int toIndex) {
        int fromRow = fromIndex / SIZE, fromCol = fromIndex % SIZE;
        int toRow = toIndex / SIZE, toCol = toIndex % SIZE;

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), tile);
        tt.setByX((toCol - fromCol) * (TILE_SIZE + 4));
        tt.setByY((toRow - fromRow) * (TILE_SIZE + 4));
        tt.play();
    }

    private boolean isAdjacent(int a, int b) {
        return (a == b - 1 && b % SIZE != 0)
                || (a == b + 1 && a % SIZE != 0)
                || (a == b - SIZE)
                || (a == b + SIZE);
    }

    private void checkWin() {
        for (int i = 0; i < tiles.size(); i++) {
            if ((int) tiles.get(i).getUserData() != i && tiles.get(i) != blankTile)
                return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "🎉 You Won!");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
