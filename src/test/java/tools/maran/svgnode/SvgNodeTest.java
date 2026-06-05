package tools.maran.svgnode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Headless tests for [SvgNode].
///
/// @author Marius Hanl
class SvgNodeTest {

    /// Small epsilon for color assertions.
    private static final double EPS = 10e-6;
    /// Square - 24x24.
    private static final String SQUARE = "M0 0h24v24H0z";
    /// Horizontal rectangle - 24x12.
    private static final String RECT_HORIZONTAL = "M0 6h24v12H0z";
    /// Vertical rectangle - 12x24.
    private static final String RECT_VERTICAL = "M6 0h12v24H6z";

    @BeforeAll
    static void initToolkit() {
        System.setProperty("glass.platform", "Headless");
        System.setProperty("prism.order", "sw");
        Platform.startup(() -> Application.setUserAgentStylesheet(toBase64("""
                .root {
                    -fx-text-base-color: black;
                }
                """)));
    }

    @Test
    @DisplayName("Changing path at runtime swaps the rendered shape")
    void testChangePath() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 48);
        node.setSvgColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.RED);

        runOnFxThread(() -> {
            node.setPath(RECT_HORIZONTAL);
            return null;
        });

        image = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 24);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 48, 12));
        assertPixelsColor(image, Color.RED, new Rectangle2D(0, 12, 48, 24));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 36, 48, 12));
    }

    @Test
    @DisplayName("Changing size at runtime rescales the SVG content")
    void testChangeSize() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setSvgColor(Color.RED);

        WritableImage smallImage = showAndSnapshot(node);
        assertSvgDimensions(node, 24, 24);
        assertPixelsColor(smallImage, Color.RED);

        runOnFxThread(() -> {
            node.setSize(48);
            return null;
        });

        WritableImage largeImage = showAndSnapshot(node);
        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(largeImage, Color.RED);
    }

    @Test
    @DisplayName("Changing SvgNode color at runtime updates rendered pixels")
    void testColorChange() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setSvgColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertPixelsColor(image, Color.RED);

        node.setSvgColor(Color.GREEN);
        image = showAndSnapshot(node);
        assertPixelsColor(image, Color.GREEN);
    }

    @Test
    @DisplayName("Setting SVG color to null clears fill to transparent")
    void testColorToNull() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 24);
        node.setSvgColor(Color.RED);

        WritableImage image = showAndSnapshot(node);
        assertPixelsColor(image, Color.RED);

        runOnFxThread(() -> {
            node.setSvgColor(null);
            return null;
        });

        image = showAndSnapshot(node);
        assertPixelsColor(image, Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Empty SvgNode renders fully transparent")
    void testDefault() throws Exception {
        SvgNode node = new SvgNode();

        WritableImage image = showAndSnapshot(node);

        assertPixelsColor(image, Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Tests the default attributes and no ill side effects in the properties")
    void testDefaultAttributes() {
        SvgNode node = new SvgNode();

        assertEquals("", node.getPath());
        assertNull(node.getSvgColor());
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
        ObjectProperty<Paint> colorProperty = node.svgColorProperty();
        assertNull(node.getSvgColor());

        colorProperty.set(Color.RED);
        assertEquals(Color.RED, node.getSvgColor());

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
    }

    @Test
    @DisplayName("Horizontal 2:1 rect scales width to size and centers vertically")
    void testHorizontalRect() throws Exception {
        SvgNode node = new SvgNode(RECT_HORIZONTAL, 48);
        node.setSvgColor(Color.RED);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 48, 24);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 48, 12));
        assertPixelsColor(image, Color.RED, new Rectangle2D(0, 12, 48, 24));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 36, 48, 12));
    }

    @Test
    @DisplayName("Square SVG at 32px renders solid blue")
    void testSquareBlue32() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 32);
        node.setSvgColor(Color.BLUE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 32, 32);
        assertPixelsColor(image, Color.BLUE);
    }

    @Test
    @DisplayName("Square SVG is correctly rendered and black due to our CSS config")
    void testSquareDefault() throws Exception {
        SvgNode node = new SvgNode(SQUARE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 24, 24);
        assertPixelsColor(image, Color.BLACK);
    }

    @Test
    @DisplayName("Square at 64px renders solid green")
    void testSquareGreen64() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 64);
        node.setSvgColor(Color.GREEN);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 64, 64);
        assertPixelsColor(image, Color.GREEN);
    }

    @Test
    @DisplayName("Square at 48px renders solid red")
    void testSquareRed48() throws Exception {
        SvgNode node = new SvgNode(SQUARE, 48);
        node.setSvgColor(Color.RED);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 48, 48);
        assertPixelsColor(image, Color.RED);
    }

    @Test
    @DisplayName("Vertical 1:2 rect scales height to size and centers horizontally")
    void testVerticalRect() throws Exception {
        SvgNode node = new SvgNode(RECT_VERTICAL, 48);
        node.setSvgColor(Color.BLUE);

        WritableImage image = showAndSnapshot(node);

        assertSvgDimensions(node, 24, 48);
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(0, 0, 12, 48));
        assertPixelsColor(image, Color.BLUE, new Rectangle2D(12, 0, 24, 48));
        assertPixelsColor(image, Color.TRANSPARENT, new Rectangle2D(36, 0, 12, 48));
    }

    private static void applyAndLayout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
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
    private static void assertSvgDimensions(SvgNode node, double expectedWidth, double expectedHeight)
            throws Exception {
        double[] widthHeightDimensions = runOnFxThread(() -> {
            Region svg = (Region) node.lookup(".svg");
            return new double[] { // Get width and height in UI thread.
                    svg.minWidth(-1), svg.prefWidth(-1), svg.maxWidth(-1), // width
                    svg.minHeight(-1), svg.prefHeight(-1), svg.maxHeight(-1) // height
            };
        });
        assertEquals(expectedWidth, widthHeightDimensions[0], 1, "svg minWidth");
        assertEquals(expectedWidth, widthHeightDimensions[1], 1, "svg prefWidth");
        assertEquals(expectedWidth, widthHeightDimensions[2], 1, "svg maxWidth");
        assertEquals(expectedHeight, widthHeightDimensions[3], 1, "svg minHeight");
        assertEquals(expectedHeight, widthHeightDimensions[4], 1, "svg prefHeight");
        assertEquals(expectedHeight, widthHeightDimensions[5], 1, "svg maxHeight");
    }

    private static Scene createScene(SvgNode node) {
        if (node.getScene() != null) {
            return node.getScene();
        }

        StackPane pane = new StackPane(node);
        Scene scene = new Scene(pane, node.getSize(), node.getSize());
        scene.setFill(Color.TRANSPARENT);
        applyAndLayout(scene);
        return scene;
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future.get(5, TimeUnit.SECONDS);
    }

    private static WritableImage showAndSnapshot(SvgNode node) throws Exception {
        return runOnFxThread(() -> {
            Scene scene = createScene(node);
            return scene.snapshot(null);
        });
    }

    private static String toBase64(String css) {
        return "data:base64," + Base64.getUrlEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }
}
