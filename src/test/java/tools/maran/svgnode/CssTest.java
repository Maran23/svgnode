package tools.maran.svgnode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javafx.application.Application;
import javafx.scene.Scene;

import org.junit.jupiter.api.BeforeAll;

/// Base class for the tests of the CSS, with a user agent stylesheet of a black text color.
///
/// @author Marius Hanl
abstract class CssTest extends JavaFxTest {

    @BeforeAll
    static void initUserAgentStylesheet() {
        runOnFxThread(() -> Application.setUserAgentStylesheet(toBase64("""
                .root {
                    -fx-text-base-color: black;
                }
                """)));
    }

    /// Applies the CSS and lays out the root of the given [Scene].
    ///
    /// @param scene
    ///         the [Scene] to apply the CSS to and lay out
    protected static void applyAndLayout(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    /// Encodes the given CSS as data URL, which can be added as stylesheet.
    ///
    /// @param css
    ///         the CSS to encode
    /// @return the data URL
    protected static String toBase64(String css) {
        return "data:base64," + Base64.getUrlEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

}
