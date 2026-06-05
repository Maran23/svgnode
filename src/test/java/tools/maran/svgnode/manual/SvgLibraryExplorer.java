package tools.maran.svgnode.manual;

import java.util.List;
import java.util.Locale;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import tools.maran.svg.SVG;
import tools.maran.svg.bootstrap.Bootstrap;
import tools.maran.svg.fontawesome.FABrand;
import tools.maran.svg.fontawesome.FARegular;
import tools.maran.svg.fontawesome.FASolid;
import tools.maran.svgnode.SvgNode;

/// An SVG explorer that displays all available SVGs from a library in a searchable, browsable grid.
///
/// Supported libraries:
/// - FontAwesome (Solid / Regular / Brand)
/// - Bootstrap
///
/// @author Marius Hanl
public class SvgLibraryExplorer {

    private final BorderPane view;

    private final Label countLabel;
    private final Label sizeLabel;
    private final TextField searchField;
    private final Slider sizeSlider;
    private final RadioButton javaRadio;

    private SvgLibrary currentLibrary;

    public SvgLibraryExplorer() {
        searchField = new TextField();
        searchField.setPromptText("Search icons...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        countLabel = new Label("0 icons");
        countLabel.setMinWidth(Region.USE_PREF_SIZE);

        HBox searchBar = new HBox(4, searchField, countLabel);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        sizeSlider = new Slider(8, 128, 20);
        sizeSlider.setSnapToTicks(true);
        sizeSlider.setBlockIncrement(1);
        sizeSlider.setMajorTickUnit(8);
        sizeSlider.setMinorTickCount(7);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setPrefWidth(400);

        sizeLabel = new Label(formatSizeLabel(20));
        sizeLabel.setMinWidth(Region.USE_PREF_SIZE);

        HBox sizeBar = new HBox(8, new Label("Size:"), sizeSlider, sizeLabel);
        sizeBar.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup copyFormatGroup = new ToggleGroup();
        javaRadio = new RadioButton("Java");
        javaRadio.setToggleGroup(copyFormatGroup);
        javaRadio.setSelected(true);

        RadioButton fxmlRadio = new RadioButton("FXML");
        fxmlRadio.setToggleGroup(copyFormatGroup);

        HBox copyBox = new HBox(8, new Label("Click any SVG to copy as"), javaRadio, fxmlRadio);
        copyBox.setAlignment(Pos.CENTER_LEFT);

        Debouncer sliderDebouncer = new Debouncer(Duration.millis(150));
        Debouncer searchDebouncer = new Debouncer(Duration.millis(250));

        sizeSlider.valueProperty().addListener((_, _, _) -> {
            sizeLabel.setText(formatSizeLabel((int) sizeSlider.getValue()));
            sliderDebouncer.run(this::resizeIcons);
        });

        searchField.textProperty().addListener((_, _, _) -> searchDebouncer.run(this::filterIcons));

        List<SvgLibrary> libraries = List.of(new BootstrapLibrary(), new FontAwesomeLibrary());

        TabPane tabPane = new TabPane();
        for (SvgLibrary lib : libraries) {
            Tab tab = new Tab(lib.name(), lib.content());
            tab.setClosable(false);
            tab.setUserData(lib);
            tabPane.getTabs().add(tab);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
            if (newTab != null) {
                selectLibrary((SvgLibrary) newTab.getUserData());
            }
        });

        StackPane stackPane = new StackPane(tabPane);
        stackPane.getStyleClass().add("content-area");

        view = new BorderPane();
        VBox top = new VBox(4, searchBar, sizeBar, copyBox);
        BorderPane.setMargin(top, new Insets(4));
        view.setTop(top);
        view.setCenter(stackPane);
        view.setPadding(new Insets(4));

        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().select(0);
    }

    public Node getView() {
        return view;
    }

    private void configureButton(Button btn) {
        double value = sizeSlider.getValue() + 24;
        btn.setMinSize(value, value);
        btn.setMaxSize(value, value);

        SvgNode graphic = (SvgNode) btn.getGraphic();
        graphic.setSize(sizeSlider.getValue());
    }

    private void copyToClipboard(String name) {
        String copyText;

        if (javaRadio.isSelected()) {
            copyText = "new SvgNode(%s.%s.path());".formatted(currentLibrary.className(), name);
        } else {
            copyText = """
                    <SvgNode>
                        <path>
                            <%s fx:constant="%s"/>
                        </path>
                    </SvgNode>
                    """.formatted(currentLibrary.className(), name);
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(copyText);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private Node createIconButton(SVG svg, String enumName) {
        SvgNode icon = new SvgNode(svg.path());

        String displayName = formatName(enumName);

        Button btn = new Button(displayName, icon);
        btn.setContentDisplay(ContentDisplay.TOP);
        btn.setUserData(displayName.toLowerCase(Locale.ROOT));
        btn.setFont(Font.font(10));
        Tooltip value = new Tooltip(displayName);
        value.setFont(Font.font(14));
        btn.setTooltip(value);
        btn.getStyleClass().add("svg-button");
        btn.setOnAction(_ -> copyToClipboard(enumName));

        return btn;
    }

    // ── Icon creation ───────────────────────────────────────────

    private void filterIcons() {
        if (currentLibrary == null) {
            return;
        }
        String filter = searchField.getText();
        if (filter != null) {
            filter = filter.strip().toLowerCase(Locale.ROOT);
        }
        int count = currentLibrary.filter(filter);
        countLabel.setText(count + " icons");
    }

    private static String formatName(String enumName) {
        return enumName.toLowerCase().replace('_', '-');
    }

    private static String formatSizeLabel(double size) {
        return size + "px";
    }

    private void resizeIcons() {
        if (currentLibrary == null) {
            return;
        }
        currentLibrary.resizeAll();
    }

    private void selectLibrary(SvgLibrary library) {
        currentLibrary = library;
        resizeIcons();
        filterIcons();
    }

    private class BootstrapLibrary extends SvgLibrary {

        BootstrapLibrary() {
            populate();
        }

        @Override
        String className() {
            return Bootstrap.class.getSimpleName();
        }

        @Override
        String name() {
            return "Bootstrap";
        }

        @Override
        SVG[] values() {
            return Bootstrap.values();
        }
    }

    /// A simple debouncer that delays execution of a [Runnable]
    /// until a specified interval has passed without further calls.
    ///
    /// @author Marius Hanl
    private record Debouncer(PauseTransition pause) {

        private Debouncer(Duration pause) {
            this(new PauseTransition(pause));
        }

        public void cancel() {
            pause.stop();
        }

        public void run(Runnable action) {
            pause.stop();
            pause.setOnFinished(_ -> action.run());
            pause.playFromStart();
        }
    }

    private class FontAwesomeLibrary extends SvgLibrary {

        private SVG[] current = FASolid.values();
        private String currentClassName = FASolid.class.getSimpleName();

        FontAwesomeLibrary() {
            ToggleGroup group = new ToggleGroup();

            ToggleButton solidToggle = new ToggleButton("Solid");
            solidToggle.getStyleClass().add("left-pill");
            solidToggle.setUserData(FASolid.class);

            ToggleButton regularToggle = new ToggleButton("Regular");
            regularToggle.getStyleClass().add("center-pill");
            regularToggle.setUserData(FARegular.class);

            ToggleButton brandToggle = new ToggleButton("Brand");
            brandToggle.getStyleClass().add("right-pill");
            brandToggle.setUserData(FABrand.class);

            solidToggle.setToggleGroup(group);
            regularToggle.setToggleGroup(group);
            brandToggle.setToggleGroup(group);

            HBox subBar = new HBox(0, solidToggle, regularToggle, brandToggle);
            subBar.setAlignment(Pos.CENTER_LEFT);
            subBar.setPadding(new Insets(4));

            content.getChildren().addFirst(subBar);

            group.selectedToggleProperty().addListener((_, _, toggle) -> {
                if (toggle == null) {
                    return;
                }
                Class<?> cls = (Class<?>) toggle.getUserData();
                if (cls == FASolid.class) {
                    current = FASolid.values();
                } else if (cls == FARegular.class) {
                    current = FARegular.values();
                } else if (cls == FABrand.class) {
                    current = FABrand.values();
                }
                currentClassName = cls.getSimpleName();
                populate();
                selectLibrary(this);
            });

            solidToggle.setSelected(true);
        }

        @Override
        String className() {
            return currentClassName;
        }

        @Override
        String name() {
            return "FontAwesome";
        }

        @Override
        SVG[] values() {
            return current;
        }
    }

    /// Base class for SVG libraries displayed in the explorer.
    ///
    /// Handles the common [FlowPane] grid, [ScrollPane] wrapping, populating, filtering, and resizing of icon buttons.
    ///
    /// @author Marius Hanl
    private abstract class SvgLibrary {

        final FlowPane svgGrid;
        final VBox content;

        SvgLibrary() {
            svgGrid = new FlowPane(2, 2);
            svgGrid.setPadding(new Insets(4));

            ScrollPane scrollPane = new ScrollPane(svgGrid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            StackPane scrollWrapper = new StackPane(scrollPane);
            VBox.setVgrow(scrollWrapper, Priority.ALWAYS);

            content = new VBox(scrollWrapper);
        }

        /// The simple class name.
        abstract String className();

        /// The content of the library.
        Node content() {
            return content;
        }

        int filter(String filterText) {
            int count = 0;

            for (Node child : svgGrid.getChildren()) {
                String name = (String) child.getUserData();
                boolean matches = filterText == null || filterText.isEmpty() || name.contains(filterText);
                child.setVisible(matches);
                child.setManaged(matches);
                if (matches) {
                    count++;
                }
            }

            return count;
        }

        /// The display name shown on the tab.
        abstract String name();

        /// Rebuilds all icon buttons from [#values()].
        void populate() {
            svgGrid.getChildren().clear();

            for (SVG svg : values()) {
                String enumName = ((Enum<?>) svg).name();
                svgGrid.getChildren().add(createIconButton(svg, enumName));
            }
        }

        /// Resizes all icon buttons to the current slider value.
        void resizeAll() {
            for (Node child : svgGrid.getChildren()) {
                configureButton((Button) child);
            }
        }

        /// The currently active set of SVG values.
        abstract SVG[] values();
    }
}
