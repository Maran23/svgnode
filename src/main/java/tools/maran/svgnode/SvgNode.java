package tools.maran.svgnode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.scene.Parent;
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
/// icon.setColor(Color.RED);
/// ```
///
/// # FXML usage
///
/// ```xml
/// <SvgNode path="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" size="32" color="RED" />
/// ```
///
/// ## Extended FXML usage from a constant (String or Enum)
///
/// ```xml
/// <SvgNode size="32" color="RED">
///     <path>
///         <MyIcons fx:constant="HOME"/>
///     </path>
/// </SvgNode>
/// ```
///
/// # CSS
///
/// ```css
/// .svg-node {
///     -fx-path: "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
///     -fx-size: 24;
///     -fx-color: red;
/// }
/// ```
///
/// @author Marius Hanl
public class SvgNode extends Parent {

    private static final double DEFAULT_SIZE = 24.0;
    private static final String DEFAULT_PATH = "";

    private final SvgContent svgContent;

    private StyleableStringProperty path;
    private StyleableDoubleProperty size;
    private StyleableObjectProperty<Paint> color;

    /// Creates an empty {@code SvgNode} with no SVG path and the default fit dimensions.
    public SvgNode() {
        getStyleClass().add("svg-node");

        svgContent = new SvgContent();
        getChildren().setAll(svgContent);
    }

    /// Creates an `SvgNode` displaying the given SVG path, rasterized to fit within a square of the default size.
    ///
    /// @param path the SVG path content (e.g. `"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"`)
    public SvgNode(String path) {
        this();
        setPath(path);
    }

    /// Creates an `SvgNode` displaying the given SVG path, rasterized to fit within a square of the specified size.
    ///
    /// @param path the SVG path content
    /// @param size the desired width and height in pixels
    public SvgNode(String path, double size) {
        this();
        setPath(path);
        setSize(size);
    }

    /// The SVG path content string (e.g. `"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"`).
    ///
    /// Can be set in CSS with `-fx-path`.
    ///
    /// @return the path property
    /// @defaultValue ""
    public final StyleableStringProperty pathProperty() {
        if (path == null) {
            path = new StyleableStringProperty(DEFAULT_PATH) {
                @Override
                protected void invalidated() {
                    svgContent.updatePath(get(), getSize());
                }

                @Override
                public CssMetaData<? extends Styleable, String> getCssMetaData() {
                    return StyleableProperties.PATH;
                }

                @Override
                public Object getBean() {
                    return SvgNode.this;
                }

                @Override
                public String getName() {
                    return "path";
                }
            };
        }
        return path;
    }

    public final String getPath() {
        return path == null ? DEFAULT_PATH : path.get();
    }

    public final void setPath(String value) {
        pathProperty().set(value);
    }

    /// The target size (width and height) the SVG should be rasterized to.
    ///
    /// The SVG path is rasterized uniformly to fit within a square of this size
    /// while preserving its original aspect ratio.
    ///
    /// Can be set in CSS with `-fx-size`.
    ///
    /// @return the size property
    /// @defaultValue 24
    public final StyleableDoubleProperty sizeProperty() {
        if (size == null) {
            size = new StyleableDoubleProperty(DEFAULT_SIZE) {
                @Override
                protected void invalidated() {
                    svgContent.updateRasterizedSize(get());
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.SIZE;
                }

                @Override
                public Object getBean() {
                    return SvgNode.this;
                }

                @Override
                public String getName() {
                    return "size";
                }
            };
        }
        return size;
    }

    public final double getSize() {
        return size == null ? DEFAULT_SIZE : size.get();
    }

    public final void setSize(double value) {
        sizeProperty().set(value);
    }

    /// The fill color of the SVG shape itself.
    ///
    /// Can be set in CSS with `-fx-color`.
    ///
    /// @return the color property
    /// @defaultValue null
    public final StyleableObjectProperty<Paint> colorProperty() {
        if (color == null) {
            color = new StyleableObjectProperty<>(null) {
                @Override
                protected void invalidated() {
                    final Paint color = get();
                    svgContent.setBackground(color == null ? null : Background.fill(color));
                }

                @Override
                public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.COLOR;
                }

                @Override
                public Object getBean() {
                    return SvgNode.this;
                }

                @Override
                public String getName() {
                    return "color";
                }
            };
        }
        return color;
    }

    public final Paint getColor() {
        return color == null ? null : color.get();
    }

    public final void setColor(Paint value) {
        colorProperty().set(value);
    }

    @Override
    public double minHeight(double width) {
        return getSize();
    }

    @Override
    public double minWidth(double height) {
        return getSize();
    }

    @Override
    public double prefHeight(double width) {
        return getSize();
    }

    @Override
    public double prefWidth(double height) {
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

    @Override
    protected void layoutChildren() {
        double nodeSize = getSize();

        double width = svgContent.svgWidth;
        double height = svgContent.svgHeight;
        double x = (nodeSize - width) / 2;
        double y = (nodeSize - height) / 2;
        svgContent.resizeRelocate(x, y, width, height);
    }

    /// Gets the {@code CssMetaData} associated with this class.
    /// Includes the {@code CssMetaData} of its superclasses.
    ///
    /// @return the {@code CssMetaData}
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    private static class StyleableProperties {

        private static final CssMetaData<SvgNode, String> PATH = new CssMetaData<>("-fx-path",
                StringConverter.getInstance(), DEFAULT_PATH) {
            @Override
            public boolean isSettable(SvgNode node) {
                return node.path == null || !node.path.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(SvgNode node) {
                return node.pathProperty();
            }
        };

        private static final CssMetaData<SvgNode, Number> SIZE = new CssMetaData<>("-fx-size",
                SizeConverter.getInstance(), DEFAULT_SIZE) {
            @Override
            public boolean isSettable(SvgNode node) {
                return node.size == null || !node.size.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(SvgNode node) {
                return node.sizeProperty();
            }
        };

        private static final CssMetaData<SvgNode, Paint> COLOR = new CssMetaData<>("-fx-color",
                PaintConverter.getInstance()) {
            @Override
            public boolean isSettable(SvgNode node) {
                return node.color == null || !node.color.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(SvgNode node) {
                return node.colorProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> metadata = Parent.getClassCssMetaData();
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(metadata.size() + 3);
            styleables.addAll(metadata);
            styleables.add(PATH);
            styleables.add(SIZE);
            styleables.add(COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /// A [Region] that uses an [SVGPath] as its shape so that it can be styled and used as normal children.
    ///
    /// @author Marius Hanl
    private static class SvgContent extends Region {

        private double svgWidth;
        private double svgHeight;

        private final SVGPath svgPath;

        SvgContent() {
            getStyleClass().add("svg");

            svgPath = new SVGPath();
            setShape(svgPath);
        }

        void updatePath(String path, double rasterizedSize) {
            svgPath.setContent(path);
            calculateSvgSize(rasterizedSize);
        }

        void updateRasterizedSize(double rasterizedSize) {
            calculateSvgSize(rasterizedSize);
        }

        @Override
        protected double computeMaxHeight(double width) {
            return svgHeight;
        }

        @Override
        protected double computeMaxWidth(double height) {
            return svgWidth;
        }

        @Override
        protected double computeMinHeight(double width) {
            return svgHeight;
        }

        @Override
        protected double computeMinWidth(double height) {
            return svgWidth;
        }

        @Override
        protected double computePrefHeight(double width) {
            return svgHeight;
        }

        @Override
        protected double computePrefWidth(double height) {
            return svgWidth;
        }

        private void calculateSvgSize(double rasterizedSize) {
            final double w = svgPath.prefWidth(-1);
            final double h = svgPath.prefHeight(-1);

            if (h > w) {
                double finalW = rasterizedSize * w;
                svgWidth = snapSizeX(finalW / h);
                svgHeight = rasterizedSize;
            } else {
                double finalH = rasterizedSize * h;
                svgHeight = snapSizeY(finalH / w);
                svgWidth = rasterizedSize;
            }
            requestLayout();
        }

        @Override
        public String getUserAgentStylesheet() {
            return SvgNode.class.getResource("svg.css").toExternalForm();
        }
    }
}
