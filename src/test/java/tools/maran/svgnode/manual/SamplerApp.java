package tools.maran.svgnode.manual;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import tools.maran.svgnode.SvgNode;

/// Manual test application for [SvgNode] and SVG libraries.
/// Provides a visual playground (sampler).
///
/// @author Marius Hanl
public class SamplerApp extends Application {

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

        Scene scene = new Scene(new StackPane(tabPane), 800, 600);
        scene.getStylesheets().add(getClass().getResource("sampler.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void setTitle(Stage stage, TabPane tabPane) {
        stage.setTitle(tabPane.getSelectionModel().getSelectedItem().getText());
    }
}
