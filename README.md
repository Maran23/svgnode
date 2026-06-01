# SvgNode

A lightweight JavaFX node for rendering SVG paths at any size. Fully supports FXML, property binding, and CSS styling.

## Features

- 🎨 Render any SVG path as a JavaFX node
- 🔗 Optimized to be efficient and have a tiny footprint due to the SvgNode extending from `Parent`, skipping size calculations and only initializing properties when needed
- 📐 Uniform rasterization with a single `size` property
- 📄 FXML-compatible with attribute and constant-based usage
- 🎭 CSS-stylable via `.svg-node` and `.svg` style classes

## Requirements

| Dependency | Version |
|------------|---------|
| Java       | 25+     |
| JavaFX     | 25+     |

## Installation

### Maven
```xml
<dependency>
    <groupId>tools.maran</groupId>
    <artifactId>svgnode</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'tools.maran:svgnode:1.0.0'
```

## Usage

### Java

```java
import tools.maran.svgnode.SvgNode;

// Default size (24px)
SvgNode small = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");

// Icon with a size
SvgNode icon = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", 32);
icon.setSvgColor(Color.RED);

// Bbinding
SvgNode dynamic = new SvgNode();
dynamic.pathProperty().bind(viewModel.iconPathProperty());
dynamic.sizeProperty().bind(slider.valueProperty());
```
### FXML

```xml
<?import tools.maran.svgnode.SvgNode?>

<SvgNode path="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" size="32" svgColor="RED" />
```

#### Using String constants from a Icon Class:
```xml
<?import tools.maran.svgnode.SvgNode?>
<?import mypackage.MyIcons?>

<SvgNode size="32" svgColor="RED">
    <path>
        <MyIcons fx:constant="HOME"/>
    </path>
</SvgNode>
```
