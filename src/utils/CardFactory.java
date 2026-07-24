package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import java.io.File;

public class CardFactory {

    /**
     * Creates a generic styled VBox card with consistent styling.
     */
    public static VBox createBaseCard(double width) {
        VBox card = new VBox();
        card.setPrefWidth(width);
        card.getStyleClass().add("movie-grid-card");
        return card;
    }

    /**
     * Creates an image region with a cover image or fallback background.
     */
    public static Region createImageRegion(String imagePath, double width, double height) {
        Region imageRegion = new Region();
        imageRegion.setPrefSize(width, height);
        imageRegion.setMinSize(width, height);
        imageRegion.setMaxSize(width, height);
        
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                String uri = file.toURI().toString().replace("'", "\\'");
                imageRegion.setStyle("-fx-background-image: url('" + uri + "'); -fx-background-size: cover; -fx-background-position: center; -fx-background-radius: 12 12 0 0; -fx-border-radius: 12 12 0 0;");
                return imageRegion;
            }
        }
        
        imageRegion.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 12 12 0 0; -fx-border-radius: 12 12 0 0;");
        return imageRegion;
    }

    /**
     * Creates a standard bold title label for a card.
     */
    public static Label createTitleLabel(String text, double fontSize, String colorHex) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        label.setTextFill(Color.web(colorHex));
        label.setWrapText(true);
        return label;
    }
}
