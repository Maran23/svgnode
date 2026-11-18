package tools.maran.svgnode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.function.Supplier;
import java.util.stream.Stream;

import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Headless tests for a [SvgNode] used as graphic of a control.
///
/// @author Marius Hanl
class SvgNodeAsGraphicTest extends CssTest {

    /// Square - 24x24.
    private static final String SQUARE = "M0 0h24v24H0z";

    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("graphics")
    @DisplayName("As a graphic, the SVG has the text color of its control")
    void testGraphicTextColor(Supplier<Parent> control, Color expected) {
        runOnFxThread(() -> {
            Parent parent = control.get();
            Scene scene = new Scene(new StackPane(parent));
            scene.getStylesheets().add(toBase64("""
                    .root {
                        -fx-text-base-color: red;
                        -fx-text-background-color: green;
                        -fx-accent: blue;
                        -fx-focused-text-base-color: yellow;
                    }
                    """));
            applyAndLayout(scene);
            applyAndLayout(scene);

            Region svg = (Region) parent.lookup(".svg");
            assertEquals(expected, svg.getBackground().getFills().getFirst().getFill());
        });
    }

    private static Arguments graphic(String name, Supplier<Parent> control, Color expected) {
        return argumentSet(name, control, expected);
    }

    /// The controls with an SVG as graphic and the text color of Modena for their text. Parts of popups and of skins
    /// that need a window are panes with the same style classes, as the color only depends on these.
    private static Stream<Arguments> graphics() {
        return Stream.of(graphic("Button", () -> new Button("Button", new SvgNode(SQUARE)), Color.RED),
                graphic("Titled pane", () -> {
                    TitledPane titledPane = new TitledPane("Title", null);
                    titledPane.setGraphic(new SvgNode(SQUARE));
                    return titledPane;
                }, Color.RED), graphic("Menu button", () -> new MenuButton("Menu", new SvgNode(SQUARE)), Color.RED),
                graphic("Combo box button cell", () -> nested("combo-box", "list-cell"), Color.RED),
                graphic("Label", () -> new Label("Label", new SvgNode(SQUARE)), Color.GREEN),
                graphic("Check box", () -> {
                    CheckBox checkBox = new CheckBox("Check");
                    checkBox.setGraphic(new SvgNode(SQUARE));
                    return checkBox;
                }, Color.GREEN), graphic("Radio button", () -> {
                    RadioButton radioButton = new RadioButton("Radio");
                    radioButton.setGraphic(new SvgNode(SQUARE));
                    return radioButton;
                }, Color.GREEN), graphic("List cell", () -> {
                    ListCell<String> cell = new ListCell<>();
                    cell.setGraphic(new SvgNode(SQUARE));
                    return cell;
                }, Color.GREEN), graphic("Nested in a cell", () -> nested("cell", "box"), Color.GREEN),
                graphic("Button in a cell", () -> nested("cell", "button"), Color.RED),
                graphic("Tooltip", () -> nested("tooltip"), Color.GREEN),
                graphic("Nested in a tooltip", () -> nested("tooltip", "box"), Color.GREEN),
                graphic("Hovered menu bar button", () -> nested("menu-bar", "container", "menu-button:hover", "label"),
                        Color.GREEN),
                graphic("Hyperlink", () -> new Hyperlink("Link", new SvgNode(SQUARE)), Color.BLUE),
                graphic("Visited hyperlink", () -> {
                    Hyperlink hyperlink = new Hyperlink("Link", new SvgNode(SQUARE));
                    hyperlink.setVisited(true);
                    return hyperlink;
                }, Color.GREEN), graphic("Menu item", () -> nested("menu-item", "graphic-container"), Color.RED),
                graphic("Menu item of a context menu on a cell", () -> nested("cell", "menu-item", "graphic-container"),
                        Color.RED),
                graphic("Focused menu item", () -> nested("menu-item:focused", "graphic-container"), Color.YELLOW),
                graphic("Label of a focused custom menu item", () -> nested("menu-item:focused", "label"),
                        Color.GREEN));
    }

    private static Parent nested(String... selectors) {
        Parent parent = new SvgNode(SQUARE);

        for (int i = selectors.length - 1; i >= 0; i--) {
            String[] parts = selectors[i].split(":");
            StackPane pane = new StackPane(parent);
            pane.getStyleClass().add(parts[0]);
            if (parts.length > 1) {
                pane.pseudoClassStateChanged(PseudoClass.getPseudoClass(parts[1]), true);
            }
            parent = pane;
        }

        return parent;
    }
}
