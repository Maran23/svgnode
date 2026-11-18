package tools.maran.svgnode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Headless tests for [SvgNode].
///
/// @author Marius Hanl
class SvgNodeTest extends CssTest {

    /// Small epsilon for color assertions.
    private static final double EPS = 10e-6;
    /// Square - 24x24.
    private static final String SQUARE = "M0 0h24v24H0z";
    /// Horizontal rectangle - 24x12.
    private static final String RECT_HORIZONTAL = "M0 6h24v12H0z";
    /// Vertical rectangle - 12x24.
    private static final String RECT_VERTICAL = "M6 0h12v24H6z";

    @Test
    @DisplayName("Changing path at runtime swaps the rendered shape")
    void testChangePath() {
        SvgNode node = new SvgNode(SQUARE, 48);
        node.setColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.RED);

        runOnFxThread(() -> node.setPath(RECT_HORIZONTAL));

        image = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 24);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 48, 12));
        assertPixelsColor(image, Color.RED, new Rectangle2D(0, 12, 48, 24));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 36, 48, 12));
    }

    @Test
    @DisplayName("Changing size at runtime rescales the SVG content")
    void testChangeSize() {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setColor(Color.RED);

        WritableImage smallImage = showAndSnapshot(node);
        assertSvgDimensions(node, 24, 24);
        assertPixelsColor(smallImage, Color.RED);

        runOnFxThread(() -> node.setSize(48));

        WritableImage largeImage = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(largeImage, Color.RED);
    }

    @Test
    @DisplayName("Changing SvgNode color at runtime updates rendered pixels")
    void testColorChange() {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertPixelsColor(image, Color.RED);

        node.setColor(Color.GREEN);
        image = showAndSnapshot(node);
        assertPixelsColor(image, Color.GREEN);
    }

    @Test
    @DisplayName("Setting SVG color to null restores the color of the stylesheet")
    void testColorToNull() {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertPixelsColor(image, Color.RED);

        runOnFxThread(() -> node.setColor(null));

        image = showAndSnapshot(node);
        assertPixelsColor(image, Color.BLACK);
    }

    @Test
    @DisplayName("Path, size and color are applied from a stylesheet")
    void testCss() {
        SvgNode node = new SvgNode();
        node.getStylesheets().add(toBase64("""
                .svg-node {
                    -fx-path: "M0 6h24v12H0z";
                    -fx-size: 48;
                    -fx-color: red;
                }
                """));

        WritableImage image = showAndSnapshot(node);

        assertEquals(RECT_HORIZONTAL, node.getPath());
        assertEquals(48, node.getSize());
        assertEquals(Color.RED, node.getColor());
        assertSvgDimensions(node, 48, 24);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 48, 12));
        assertPixelsColor(image, Color.RED, new Rectangle2D(0, 12, 48, 24));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 36, 48, 12));
    }

    @Test
    @DisplayName("Removing the style class of a color restores the color of the stylesheet")
    void testCssColorRemoved() {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.getStylesheets().add(toBase64("""
                .red {
                    -fx-color: red;
                }
                """));
        node.getStyleClass().add("red");

        WritableImage image = showAndSnapshot(node);
        assertPixelsColor(image, Color.RED);

        runOnFxThread(() -> node.getStyleClass().remove("red"));

        image = showAndSnapshot(node);
        assertNull(node.getColor());
        assertPixelsColor(image, Color.BLACK);
    }

    @Test
    @DisplayName("CSS does not override bound properties")
    void testCssIgnoredWhenBound() {
        SvgNode node = new SvgNode();
        node.pathProperty().bind(new SimpleStringProperty(SQUARE));
        node.sizeProperty().bind(new SimpleDoubleProperty(48));
        node.colorProperty().bind(new SimpleObjectProperty<>(Color.RED));
        node.setStyle("-fx-path: \"%s\"; -fx-size: 32; -fx-color: blue;".formatted(RECT_VERTICAL));

        WritableImage image = showAndSnapshot(node);

        assertEquals(SQUARE, node.getPath());
        assertEquals(48, node.getSize());
        assertEquals(Color.RED, node.getColor());
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.RED);
    }

    @Test
    @DisplayName("Inline style overrides values set in code")
    void testCssInlineOverridesSetValues() {
        SvgNode node = new SvgNode(RECT_VERTICAL, 24);
        node.setColor(Color.RED);
        node.setStyle("-fx-path: \"%s\"; -fx-size: 48; -fx-color: blue;".formatted(SQUARE));

        WritableImage image = showAndSnapshot(node);

        assertEquals(SQUARE, node.getPath());
        assertEquals(48, node.getSize());
        assertEquals(Color.BLUE, node.getColor());
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.BLUE);
    }

    @Test
    @DisplayName("CSS size in em is relative to the parent font size")
    void testCssSizeEm() {
        SvgNode node = new SvgNode(SQUARE);
        node.setStyle("-fx-size: 2em;");
        StackPane parent = new StackPane(node);
        parent.setStyle("-fx-font-size: 20px;");

        runOnFxThread(() -> applyAndLayout(new Scene(parent)));
        assertEquals(40, node.getSize(), EPS);

        runOnFxThread(() -> {
            parent.setStyle("-fx-font-size: 10px;");
            applyAndLayout(parent.getScene());
        });
        assertEquals(20, node.getSize(), EPS);
    }

    @Test
    @DisplayName("Empty SvgNode renders fully transparent")
    void testDefault() {
        SvgNode node = new SvgNode();

        WritableImage image = showAndSnapshot(node);

        assertPixelsColor(image, Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Tests the default attributes and no ill side effects in the properties")
    void testDefaultAttributes() {
        SvgNode node = new SvgNode();

        assertEquals("", node.getPath());
        assertNull(node.getColor());
        assertEquals(24, node.getSize());

        assertEquals(24, node.minWidth(-1));
        assertEquals(24, node.prefWidth(-1));
        assertEquals(24, node.computePrefWidth(-1));

        assertEquals(24, node.minHeight(-1));
        assertEquals(24, node.prefHeight(-1));
        assertEquals(24, node.computePrefHeight(-1));

        // Initialize property but do not use yet.
        StringProperty pathProperty = node.pathProperty();
        assertEquals("", node.getPath());

        pathProperty.set("M 0 0z");
        assertEquals("M 0 0z", node.getPath());

        // Initialize property but do not use yet.
        ObjectProperty<Paint> colorProperty = node.colorProperty();
        assertNull(node.getColor());

        colorProperty.set(Color.RED);
        assertEquals(Color.RED, node.getColor());

        // Initialize property but do not use yet.
        DoubleProperty sizeProperty = node.sizeProperty();
        assertEquals(24, node.getSize());

        sizeProperty.set(32);
        assertEquals(32, node.getSize());

        assertEquals(32, node.minWidth(-1));
        assertEquals(32, node.prefWidth(-1));
        assertEquals(32, node.computePrefWidth(-1));

        assertEquals(32, node.minHeight(-1));
        assertEquals(32, node.prefHeight(-1));
        assertEquals(32, node.computePrefHeight(-1));

        assertEquals(node, pathProperty.getBean());
        assertEquals("path", pathProperty.getName());
        assertEquals(node, colorProperty.getBean());
        assertEquals("color", colorProperty.getName());
        assertEquals(node, sizeProperty.getBean());
        assertEquals("size", sizeProperty.getName());

        assertEquals("-fx-path", node.pathProperty().getCssMetaData().getProperty());
        assertEquals("-fx-color", node.colorProperty().getCssMetaData().getProperty());
        assertEquals("-fx-size", node.sizeProperty().getCssMetaData().getProperty());
    }

    @Test
    @DisplayName("Horizontal 2:1 rect scales width to size and centers vertically")
    void testHorizontalRect() {
        SvgNode node = new SvgNode(RECT_HORIZONTAL, 48);
        node.setColor(Color.RED);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 48, 24);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 48, 12));
        assertPixelsColor(image, Color.RED, new Rectangle2D(0, 12, 48, 24));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 36, 48, 12));
    }

    @Test
    @DisplayName("Null path renders fully transparent")
    void testNullPath() {
        SvgNode node = new SvgNode(SQUARE);
        node.setColor(Color.RED);
        node.setPath(null);

        WritableImage image = showAndSnapshot(node);

        assertNull(node.getPath());
        assertPixelsColor(image, Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Square SVG at 32px renders solid blue")
    void testSquareBlue32() {
        SvgNode node = new SvgNode(SQUARE, 32);
        node.setColor(Color.BLUE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 32, 32);
        assertPixelsColor(image, Color.BLUE);
    }

    @Test
    @DisplayName("Square SVG is correctly rendered and black due to our CSS config")
    void testSquareDefault() {
        SvgNode node = new SvgNode(SQUARE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 24, 24);
        assertPixelsColor(image, Color.BLACK);
    }

    @Test
    @DisplayName("Square at 64px renders solid green")
    void testSquareGreen64() {
        SvgNode node = new SvgNode(SQUARE, 64);
        node.setColor(Color.GREEN);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 64, 64);
        assertPixelsColor(image, Color.GREEN);
    }

    @Test
    @DisplayName("Square at 48px renders solid red")
    void testSquareRed48() {
        SvgNode node = new SvgNode(SQUARE, 48);
        node.setColor(Color.RED);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.RED);
    }

    @Test
    @DisplayName("Vertical 1:2 rect scales height to size and centers horizontally")
    void testVerticalRect() {
        SvgNode node = new SvgNode(RECT_VERTICAL, 48);
        node.setColor(Color.BLUE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 24, 48);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 12, 48));
        assertPixelsColor(image, Color.BLUE, new Rectangle2D(12, 0, 24, 48));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(36, 0, 12, 48));
    }

    /// Asserts that all pixels in the image match the expected color.
    private static void assertPixelsColor(WritableImage image, Color expected) {
        assertPixelsColor(image, expected, new Rectangle2D(0, 0, image.getWidth(), image.getHeight()));
    }

    /// Asserts that all pixels within the given [Rectangle2D] match the expected color.
    private static void assertPixelsColor(WritableImage image, Color expected, Rectangle2D rect) {
        PixelReader reader = image.getPixelReader();
        int x1 = (int) rect.getMinX();
        int y1 = (int) rect.getMinY();
        int x2 = (int) (rect.getMinX() + rect.getWidth());
        int y2 = (int) (rect.getMinY() + rect.getHeight());

        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                Color actual = reader.getColor(x, y);
                assertEquals(expected.getRed(), actual.getRed(), EPS, "red mismatch at (%d,%d)".formatted(x, y));
                assertEquals(expected.getGreen(), actual.getGreen(), EPS, "green mismatch at (%d,%d)".formatted(x, y));
                assertEquals(expected.getBlue(), actual.getBlue(), EPS, "blue mismatch at (%d,%d)".formatted(x, y));
                assertEquals(expected.getOpacity(), actual.getOpacity(), EPS,
                        "opacity mismatch at (%d,%d)".formatted(x, y));
            }
        }
    }

    /// Asserts that the inner SVG [Region] (styleClass `svg`) reports the expected dimensions
    /// across all six compute methods.
    private static void assertSvgDimensions(SvgNode node, double expectedWidth, double expectedHeight) {
        double[] widthHeightDimensions = getOnFxThread(() -> {
            Region svg = (Region) node.lookup(".svg");
            return new double[] { // Get width and height in UI thread.
                    svg.minWidth(-1), svg.prefWidth(-1), svg.maxWidth(-1), // width
                    svg.minHeight(-1), svg.prefHeight(-1), svg.maxHeight(-1) // height
            };
        });
        assertEquals(expectedWidth, widthHeightDimensions[0], EPS, "svg minWidth");
        assertEquals(expectedWidth, widthHeightDimensions[1], EPS, "svg prefWidth");
        assertEquals(expectedWidth, widthHeightDimensions[2], EPS, "svg maxWidth");
        assertEquals(expectedHeight, widthHeightDimensions[3], EPS, "svg minHeight");
        assertEquals(expectedHeight, widthHeightDimensions[4], EPS, "svg prefHeight");
        assertEquals(expectedHeight, widthHeightDimensions[5], EPS, "svg maxHeight");
    }

    private static WritableImage showAndSnapshot(SvgNode node) {
        return getOnFxThread(() -> {
            Scene scene = useScene(node);
            return scene.snapshot(null);
        });
    }

    private static Scene useScene(SvgNode node) {
        if (node.getScene() != null) {
            return node.getScene();
        }

        StackPane pane = new StackPane(node);
        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);
        applyAndLayout(scene);
        return scene;
    }
}
