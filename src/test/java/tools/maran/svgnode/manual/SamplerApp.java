package tools.maran.svgnode.manual;

import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import tools.maran.shadfx.Theme;
import tools.maran.svgnode.SvgNode;

/// Manual test application for [SvgNode] and SVG libraries.
/// Provides a visual playground (sampler).
///
/// @author Marius Hanl
public class SamplerApp extends Application {

    private static final String MODENA = "Modena";
    private static final String SHADFX = "ShadFX";

    public static void main() {
        launch();
    }

    @Override
    public void start(Stage stage) {
        Tab samplerTab = new Tab("SvgNode Sampler");
        samplerTab.setContent(new SvgNodeSampler().getView());
        samplerTab.setClosable(false);

        Tab explorerTab = new Tab("SVG Library Explorer");
        explorerTab.setContent(new SvgLibraryExplorer().getView());
        explorerTab.setClosable(false);

        TabPane tabPane = new TabPane();
        tabPane.getSelectionModel().selectedItemProperty().addListener(_ -> setTitle(stage, tabPane));
        tabPane.getTabs().setAll(samplerTab, explorerTab);

        BorderPane root = new BorderPane(tabPane);
        Scene scene = new Scene(root, 800, 600);
        root.setTop(createHeader(scene));
        stage.setScene(scene);
        stage.show();
    }

    /// Creates the header to switch the theme of the scene, Modena by default.
    private static ToolBar createHeader(Scene scene) {
        ChoiceBox<String> theme = new ChoiceBox<>(FXCollections.observableArrayList(MODENA, SHADFX));
        theme.setValue(MODENA);
        theme.valueProperty().subscribe(value -> scene.getStylesheets()
                .setAll(SHADFX.equals(value) ? Theme.of().stylesheets() : List.of()));

        Label label = new Label("Theme");
        label.setLabelFor(theme);

        return new ToolBar(label, theme);
    }

    private void setTitle(Stage stage, TabPane tabPane) {
        stage.setTitle(tabPane.getSelectionModel().getSelectedItem().getText());
    }
}
