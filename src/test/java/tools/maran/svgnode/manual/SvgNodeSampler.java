package tools.maran.svgnode.manual;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import tools.maran.svgnode.SvgNode;

/// Sampler for the [SvgNode]:
///
/// - Label, Button Graphic + Transition
/// - Static SVGs with Tooltips
/// - Runtime path swapping
/// - Runtime color changes
/// - Runtime resize behavior
///
/// @author Marius Hanl
public class SvgNodeSampler {

    private static final String HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String MAIL = "M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z";
    private static final String FILTER = "M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z";
    private static final String STAR = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    private static final String HEART = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z";

    private static final List<Named<String>> PATHS = List.of(
            new Named<>("Home", HOME),
            new Named<>("Mail", MAIL),
            new Named<>("Filter", FILTER),
            new Named<>("Star", STAR),
            new Named<>("Heart", HEART)
    );

    private static final List<Named<Color>> COLORS = List.of(
            new Named<>("Blue", Color.DODGERBLUE),
            new Named<>("Red", Color.CRIMSON),
            new Named<>("Green", Color.FORESTGREEN),
            new Named<>("Orange", Color.ORANGE),
            new Named<>("Purple", Color.MEDIUMPURPLE)
    );

    private static final List<Named<Double>> SIZES = List.of(
            new Named<>("16px", 16d),
            new Named<>("24px", 24d),
            new Named<>("32px", 32d),
            new Named<>("48px", 48d),
            new Named<>("64px", 64d),
            new Named<>("128px", 128d)
    );

    private final VBox view;

    public SvgNodeSampler() {
        view = new VBox(4);
        view.setPadding(new Insets(4));
        view.setAlignment(Pos.TOP_LEFT);

        addLabels();
        addButtonGraphics();
        addStaticSvgs();
        addSvgPathSwap();
        addSvgColorChange();
        addSvgResize();
    }

    public Node getView() {
        return view;
    }

    private void addButtonGraphics() {
        view.getChildren().add(sectionLabel("Button Graphics automatically adjust"));

        Button buttonLight = createButtonWithCss("Light -> Dark on Hover (CSS)", """
                .button {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                .button:hover {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """);

        Button buttonLight2 = createButtonWithCss("Light -> Dark on Hover with Transition (CSS)", """
                .button {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                    transition-property: -fx-background-color, -fx-text-fill;
                    transition-duration: 1000ms;
                }
                .button .svg {
                    transition-property: -fx-background-color;
                    transition-duration: 1000ms;
                }
                .button:hover {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """);

        view.getChildren().add(new HBox(4, buttonLight, buttonLight2));

        Button buttonDark = createButtonWithCss("Dark -> Light on Hover (CSS)", """
                .button {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                .button:hover {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """);

        Button buttonDark2 = createButtonWithCss("Dark -> Light on Hover with Transition (CSS)", """
                .button {
                    -fx-color: #000000; -fx-background: -fx-color; -fx-background-color: -fx-background;
                    transition-property: -fx-background-color;
                    transition-duration: 1000ms;
                }
                .button .svg {
                    transition-property: -fx-background-color;
                    transition-duration: 1000ms;
                }
                .button:hover {
                    -fx-color: #FFFFFF; -fx-background: -fx-color; -fx-background-color: -fx-background;
                }
                """);
        view.getChildren().add(new HBox(4, buttonDark, buttonDark2));
    }

    private void addLabels() {
        view.getChildren().add(sectionLabel("Usage with Label, CSS (Hover), Background, Border"));

        Label label = new Label("SVG with text", new SvgNode(HEART));

        SvgNode svgNodeHover = new SvgNode();
        svgNodeHover.getStylesheets().add(toBase64("""
                .svg-node {
                    -fx-path: "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
                    -fx-size: 24;
                    -fx-color: red;
                }
                
                .svg-node:hover {
                    -fx-path: "M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z";
                    -fx-size: 32;
                    -fx-color: blue;
                }
                """));

        SvgNode svgNodeBg = new SvgNode(HEART);
        svgNodeBg.setColor(Color.RED);
        svgNodeBg.setBackground(Background.fill(Color.MISTYROSE));
        svgNodeBg.setBorder( new Border(new BorderStroke(Color.LIGHTGREEN, BorderStrokeStyle.SOLID, null, BorderStroke.THICK)));

        view.getChildren().add(new HBox(4, label, svgNodeHover, svgNodeBg));
    }

    private void addStaticSvgs() {
        view.getChildren().addAll(new Separator(), sectionLabel("Static icons with Tooltip (24px)"));

        HBox staticRow = new HBox(12);
        staticRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < PATHS.size(); i++) {
            Named<String> path = PATHS.get(i);
            Named<Color> color = COLORS.get(i);

            SvgNode icon = new SvgNode(path.value());
            icon.setColor(color.value());
            Tooltip.install(icon, new Tooltip(path.label()));
            staticRow.getChildren().addAll(icon, new Label(path.label()));
        }
        view.getChildren().add(staticRow);
    }

    private void addSvgColorChange() {
        view.getChildren().addAll(new Separator(), sectionLabel("Runtime color change"));

        SvgNode colorTarget = new SvgNode(HEART, 40);
        colorTarget.setColor(Color.CRIMSON);

        HBox toggleBar = createToggleBar(COLORS, 1, colorTarget::setColor);
        view.getChildren().add(new HBox(12, colorTarget, toggleBar));
    }

    private void addSvgPathSwap() {
        view.getChildren().addAll(new Separator(), sectionLabel("Runtime path swap"));

        SvgNode swappable = new SvgNode(HOME, 32);
        swappable.setColor(Color.DODGERBLUE);

        HBox toggleBar = createToggleBar(PATHS, 0, swappable::setPath);
        view.getChildren().add(new HBox(12, swappable, toggleBar));
    }

    private void addSvgResize() {
        view.getChildren().addAll(new Separator(), sectionLabel("Runtime resize"));

        SvgNode resizable = new SvgNode(STAR, 24);
        resizable.setColor(Color.ORANGE);

        HBox toggleBar = createToggleBar(SIZES, 1, resizable::setSize);
        view.getChildren().add(new VBox(4, toggleBar, resizable));
    }

    private Button createButtonWithCss(String text, String css) {
        Button button = new Button(text, new SvgNode(HEART));
        button.setCursor(Cursor.HAND);
        button.getStylesheets().add(toBase64(css));
        return button;
    }

    private <T> HBox createToggleBar(List<Named<T>> items, int defaultIndex, Consumer<T> onSelect) {
        HBox box = new HBox(0);
        box.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener((_, _, toggle) -> {
            if (toggle == null) {
                return;
            }
            onSelect.accept((T) toggle.getUserData());
        });

        for (int i = 0; i < items.size(); i++) {
            Named<T> item = items.get(i);
            ToggleButton btn = new ToggleButton(item.label());
            btn.setUserData(item.value());
            btn.setToggleGroup(group);

            if (i == 0) {
                btn.getStyleClass().add("left-pill");
            } else if (i == items.size() - 1) {
                btn.getStyleClass().add("right-pill");
            } else {
                btn.getStyleClass().add("center-pill");
            }

            box.getChildren().add(btn);
        }

        ((ToggleButton) box.getChildren().get(defaultIndex)).setSelected(true);
        return box;
    }

    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("header");
        return label;
    }

    private static String toBase64(String css) {
        return "data:base64," + Base64.getUrlEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

    private record Named<T>(String label, T value) { }
}
