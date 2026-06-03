package tools.maran.svgnode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/// Manual test application for [SvgNode].
///
/// Provides a visual playground to verify:
///
/// - Button Graphic + Transition
/// - SVG rendering at various sizes
/// - Runtime path swapping
/// - Runtime color changes
/// - Runtime resize behavior
///
/// @author Marius Hanl
public class SvgNodeManualTest extends Application {

    private static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String MAIL = "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z";
    private static final String FILTER = "M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z";
    private static final String STAR = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    private static final String HEART = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z";

    private static final String[] ALL_PATHS = { HOME, MAIL, FILTER, STAR, HEART };
    private static final String[] PATH_NAMES = { "Home", "Mail", "Filter", "Star", "Heart" };
    private static final Color[] COLORS = { Color.DODGERBLUE, Color.CRIMSON, Color.FORESTGREEN, Color.ORANGE, Color.MEDIUMPURPLE };
    private static final String[] COLOR_NAMES = { "Blue", "Red", "Green", "Orange", "Purple" };
    private static final Double[] SIZES = { 16d, 24d, 32d, 48d, 64d, 128d };
    private static final String[] SIZE_LABELS = { "16px", "24px", "32px", "48px", "64px", "128px" };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        buttonGraphics(root);
        staticSvgs(root);
        svgPathSwap(root);
        svgColorChange(root);
        svgResize(root);

        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("SvgNode – Manual Test");
        stage.setScene(scene);
        stage.show();
    }

    private void buttonGraphics(VBox root) {
        root.getChildren().add(sectionLabel("Button Graphics automatically adjust + Transition"));
        Button buttonLight = new Button("Button Light with Dark Hover", new SvgNode(HEART));
        buttonLight.getStylesheets().add(toBase64("""
                .button {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                    transition-property: -fx-background-color;
                    transition-duration: 400ms;
                }
                .button:hover {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """));
        root.getChildren().add(buttonLight);

        Button buttonDark = new Button("Button Dark with Light Hover", new SvgNode(HEART));
        buttonDark.getStylesheets().add(toBase64("""
                .button {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                    transition-property: -fx-background-color;
                    transition-duration: 400ms;
                }
                .button:hover {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """));
        root.getChildren().add(buttonDark);
    }

    /// Creates an [HBox] of mutually exclusive [ToggleButton]s.
    /// When the selection changes the supplied `onSelect` callback is
    /// invoked with the value associated with the chosen toggle.
    ///
    /// @param <T>          value type stored as user data on each toggle
    /// @param values       one value per toggle
    /// @param labels       display text for each toggle (parallel to `values`)
    /// @param defaultIndex index of the toggle that should be initially selected
    /// @param onSelect     callback receiving the selected value
    /// @return an [HBox] containing the toggle buttons
    private <T> HBox createToggleBar(T[] values, String[] labels, int defaultIndex, Consumer<T> onSelect) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener(_ -> {
            Toggle toggle = group.getSelectedToggle();
            if (toggle == null) {
                return;
            }
            T userData = (T) toggle.getUserData();
            onSelect.accept(userData);
        });

        for (int i = 0; i < values.length; i++) {
            ToggleButton btn = new ToggleButton(labels[i]);
            btn.setUserData(values[i]);
            btn.setToggleGroup(group);
            box.getChildren().add(btn);
        }

        ((ToggleButton) box.getChildren().get(defaultIndex)).setSelected(true);

        return box;
    }

    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private void staticSvgs(VBox root) {
        root.getChildren().addAll(new Separator(), sectionLabel("Static icons with Tooltip (24px)"));

        HBox staticRow = new HBox(12);
        staticRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < ALL_PATHS.length; i++) {
            SvgNode icon = new SvgNode(ALL_PATHS[i]);
            icon.setSvgColor(COLORS[i]);
            Tooltip.install(icon, new Tooltip("Tooltip"));
            staticRow.getChildren().addAll(icon, new Label(PATH_NAMES[i]));
        }
        root.getChildren().add(staticRow);
    }

    private void svgColorChange(VBox root) {
        root.getChildren().addAll(new Separator(), sectionLabel("Runtime color change"));

        SvgNode colorTarget = new SvgNode(HEART, 40);
        colorTarget.setSvgColor(Color.CRIMSON);

        HBox toggleBar = createToggleBar(COLORS, COLOR_NAMES, 1, colorTarget::setSvgColor);
        root.getChildren().add(new HBox(12, colorTarget, toggleBar));
    }

    private void svgPathSwap(VBox root) {
        root.getChildren().addAll(new Separator(), sectionLabel("Runtime path swap"));

        SvgNode swappable = new SvgNode(HOME, 32);
        swappable.setSvgColor(Color.DODGERBLUE);

        HBox toggleBar = createToggleBar(ALL_PATHS, PATH_NAMES, 0, swappable::setPath);
        root.getChildren().add(new HBox(12, swappable, toggleBar));
    }

    private void svgResize(VBox root) {
        root.getChildren().addAll(new Separator(), sectionLabel("Runtime resize"));

        SvgNode resizable = new SvgNode(STAR, 24);
        resizable.setSvgColor(Color.ORANGE);

        HBox toggleBar = createToggleBar(SIZES, SIZE_LABELS, 1, resizable::setSize);
        root.getChildren().add(new HBox(12, resizable, toggleBar));
    }

    private String toBase64(String css) {
        return "data:base64," + Base64.getUrlEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }
}
