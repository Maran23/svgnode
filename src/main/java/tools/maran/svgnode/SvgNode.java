package tools.maran.svgnode;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/// Node to show an SVG path with the specified size.
///
/// # Java usage
///
/// ```java
/// SvgNode icon = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", 32);
/// icon.setSvgColor(Color.RED);
/// ```
///
/// # FXML usage
///
/// ```xml
/// <SvgNode path="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" size="32" svgColor="RED" />
/// ```
///
/// ## Extended FXML usage from a constant (String or Enum)
///
/// ```xml
/// <SvgNode size="32" svgColor="RED">
///     <path>
///         <MyIcons fx:constant="HOME"/>
///     </path>
/// </SvgNode>
/// ```
///
/// @author Marius Hanl
public class SvgNode extends Region {

    private static final double DEFAULT_SIZE = 24.0;
    private static final String DEFAULT_PATH = "";

    private StringProperty path;
    private DoubleProperty size;
    private ObjectProperty<Paint> svgColor;
    private final SVGPath svgPath;

    /// Creates an empty {@code SvgNode} with no SVG path and the default fit dimensions.
    public SvgNode() {
        getStyleClass().add("svg-node");

        svgPath = new SVGPath();
        setShape(svgPath);
    }

    /// Creates an `SvgNode` displaying the given SVG path, rasterized to fit within a square of the default size.
    ///
    /// @param path
    ///         the SVG path content (e.g. `"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"`)
    public SvgNode(String path) {
        this();
        setPath(path);
    }

    /// Creates an `SvgNode` displaying the given SVG path, rasterized to fit within a square of the specified size.
    ///
    /// @param path
    ///         the SVG path content
    /// @param size
    ///         the desired width and height in pixels
    public SvgNode(String path, double size) {
        this();
        setPath(path);
        setSize(size);
    }

    public final String getPath() {
        return path == null ? DEFAULT_PATH : path.get();
    }

    public final double getSize() {
        return size == null ? DEFAULT_SIZE : size.get();
    }

    @Deprecated(forRemoval = true, since = "1.1.0")
    public final Paint getSvgColor() {
        return svgColor == null ? null : svgColor.get();
    }

    @Override
    public String getUserAgentStylesheet() {
        return SvgNode.class.getResource("svg-node.css").toExternalForm();
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    /// The SVG path content string (e.g. `"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"`).
    ///
    /// @return the path property
    public final StringProperty pathProperty() {
        if (path == null) {
            path = new SimpleStringProperty(this, "path", DEFAULT_PATH) {
                @Override
                protected void invalidated() {
                    svgPath.setContent(get());
                    recalculateBounds();
                }
            };
        }
        return path;
    }

    public final void setPath(String value) {
        pathProperty().set(value);
    }

    public final void setSize(double value) {
        sizeProperty().set(value);
    }

    @Deprecated(forRemoval = true, since = "1.1.0")
    public final void setSvgColor(Paint value) {
        svgColorProperty().set(value);
    }

    /// The target size (width and height) the SVG should be rasterized to.
    ///
    /// The SVG path is rasterized uniformly to fit within a square of this size
    /// while preserving its original aspect ratio.
    ///
    /// @return the size property
    /// @defaultValue 24
    public final DoubleProperty sizeProperty() {
        if (size == null) {
            size = new SimpleDoubleProperty(this, "size", DEFAULT_SIZE) {
                @Override
                protected void invalidated() {
                    recalculateBounds();
                    requestLayout();
                }
            };
        }
        return size;
    }

    /// The fill color of the SVG shape itself.
    ///
    /// @return the svgColor property
    @Deprecated(forRemoval = true, since = "1.1.0")
    public final ObjectProperty<Paint> svgColorProperty() {
        if (svgColor == null) {
            svgColor = new SimpleObjectProperty<>(this, "svgColor") {
                @Override
                protected void invalidated() {
                    final Paint color = get();
                    setBackground(color == null ? null : Background.fill(color));
                }
            };
        }
        return svgColor;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return getSize();
    }

    @Override
    protected double computeMaxWidth(double height) {
        return getSize();
    }

    @Override
    protected double computeMinHeight(double width) {
        return getSize();
    }

    @Override
    protected double computeMinWidth(double height) {
        return getSize();
    }

    @Override
    protected double computePrefHeight(double width) {
        return getSize();
    }

    @Override
    protected double computePrefWidth(double height) {
        return getSize();
    }

    private void recalculateBounds() {
        double rasterizedSize = getSize();

        final double w = svgPath.prefWidth(-1);
        final double h = svgPath.prefHeight(-1);

        double svgWidth;
        double svgHeight;

        double finalW = rasterizedSize * w;
        double finalH = rasterizedSize * h;
        if (finalH > finalW) {
            svgWidth = Math.round(finalW / h);
            svgHeight = rasterizedSize;
        } else {
            svgHeight = Math.round(finalH / w);
            svgWidth = rasterizedSize;
        }

        double x = (rasterizedSize - svgWidth) / 2;
        double y = (rasterizedSize - svgHeight) / 2;

        resizeRelocate(x, y, svgWidth, svgHeight);
    }
}
