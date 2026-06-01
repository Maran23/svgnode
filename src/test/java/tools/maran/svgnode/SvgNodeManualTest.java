package tools.maran.svgnode;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Manual test application for {@link SvgNode}.
 *
 * <p>Provides a visual playground to verify:</p>
 * <ul>
 *     <li>SVG rendering at various sizes</li>
 *     <li>Dynamic path swapping</li>
 *     <li>Runtime color changes</li>
 *     <li>Runtime resize behavior</li>
 * </ul>
 *
 * <p>Run as a standard JavaFX {@link Application}.</p>
 */
public class SvgNodeManualTest extends Application {

    private static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String MAIL = "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z";
    private static final String FILTER = "M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z";
    private static final String STAR = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    private static final String HEART = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z";

    private static final String[] ALL_PATHS = { HOME, MAIL, FILTER, STAR, HEART };
    private static final String[] PATH_NAMES = { "Home", "Mail", "Filter", "Star", "Heart" };
    private static final Color[] COLORS = { Color.DODGERBLUE, Color.CRIMSON, Color.FORESTGREEN, Color.ORANGE,
            Color.MEDIUMPURPLE };

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        root.getChildren().add(sectionLabel("Button Graphic"));
        Button buttonLight = new Button("Button Light", new SvgNode(HEART));
        buttonLight.setStyle("-fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;");
        root.getChildren().add(buttonLight);

        Button buttonDark = new Button("Button Dark", new SvgNode(HEART));
        buttonDark.setStyle("-fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;");
        root.getChildren().add(buttonDark);

        root.getChildren().addAll(new Separator(), sectionLabel("Static icons (24 × 24)"));

        HBox staticRow = new HBox(12);
        staticRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < ALL_PATHS.length; i++) {
            SvgNode icon = new SvgNode(ALL_PATHS[i]);
            icon.setSvgColor(COLORS[i]);
            staticRow.getChildren().addAll(icon, new Label(PATH_NAMES[i]));
        }
        root.getChildren().add(staticRow);

        root.getChildren().addAll(new Separator(), sectionLabel("Dynamic path swap"));

        SvgNode swappable = new SvgNode(HOME, 32);
        swappable.setSvgColor(Color.DODGERBLUE);

        HBox swapButtons = new HBox(8);
        swapButtons.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < ALL_PATHS.length; i++) {
            final String path = ALL_PATHS[i];
            Button btn = new Button(PATH_NAMES[i]);
            btn.setOnAction(_ -> swappable.setPath(path));
            swapButtons.getChildren().add(btn);
        }
        root.getChildren().addAll(new HBox(12, swappable, swapButtons));

        root.getChildren().addAll(new Separator(), sectionLabel("Runtime color change"));

        SvgNode colourTarget = new SvgNode(HEART, 40);
        colourTarget.setSvgColor(Color.CRIMSON);

        HBox colourButtons = new HBox(8);
        colourButtons.setAlignment(Pos.CENTER_LEFT);
        for (Color color : COLORS) {
            Button btn = new Button(colorName(color));
            btn.setOnAction(_ -> colourTarget.setSvgColor(color));
            colourButtons.getChildren().add(btn);
        }
        root.getChildren().addAll(new HBox(12, colourTarget, colourButtons));

        root.getChildren().addAll(new Separator(), sectionLabel("Runtime resize"));

        SvgNode resizable = new SvgNode(STAR, 24);
        resizable.setSvgColor(Color.ORANGE);

        HBox sizeButtons = new HBox(8);
        sizeButtons.setAlignment(Pos.CENTER_LEFT);
        for (int size : new int[] { 16, 24, 32, 48, 64, 128 }) {
            Button btn = new Button(size + "px");
            btn.setOnAction(_ -> resizable.setSize(size));
            sizeButtons.getChildren().add(btn);
        }
        root.getChildren().addAll(new HBox(12, resizable, sizeButtons));

        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("SvgNode – Manual Test");
        stage.setScene(scene);
        stage.show();
    }

    /** Creates a bold section header label. */
    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    /** Returns a readable name for well-known colors. */
    private static String colorName(Color color) {
        if (color.equals(Color.DODGERBLUE)) {
            return "Blue";
        }
        if (color.equals(Color.CRIMSON)) {
            return "Red";
        }
        if (color.equals(Color.FORESTGREEN)) {
            return "Green";
        }
        if (color.equals(Color.ORANGE)) {
            return "Orange";
        }
        if (color.equals(Color.MEDIUMPURPLE)) {
            return "Purple";
        }
        return color.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
