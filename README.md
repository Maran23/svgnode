# SvgNode

A lightweight, optimized JavaFX node for rendering SVG paths at any size. Fully supports FXML, property binding, and CSS styling.

![Sampler](https://github.com/Maran23/svgnode/blob/demo/demo/sampler.webp?raw=true)

<details>
<summary>Usage with SVG Libraries</summary>

## SVG Libraries

### Material Design
![Material Design](https://github.com/Maran23/svgnode/blob/demo/demo/library_materialdesign.webp)

### Bootstrap
![Bootstrap](https://github.com/Maran23/svgnode/blob/demo/demo/library_bootstrap.webp)

### Font Awesome
![FontAwesome](https://github.com/Maran23/svgnode/blob/demo/demo/library_fontawesome.webp)

</details>

## Features

- 🎨 Render any SVG path as a JavaFX node
- 🔗 No dependencies – will use your provided JavaFX runtime
- ⚡ Optimized to be efficient and have a tiny footprint due to the SvgNode extending from `Parent`, skipping size calculations and only initializing properties when needed
- 📐 Uniform rasterization with a single `size` property
- 📄 FXML-compatible with attribute and constant-based usage
- 🎭 CSS-stylable via `.svg-node` and `.svg` style classes. By default, the SVG automatically adjusts its color based on the background – just like text!

### With SVG Libraries

Will work fine with SVG Libraries (see usage [below](#use-with-svg-libraries)) such as:
- [SVG-MaterialDesign](https://github.com/Maran23/svg-materialdesign)
- [SVG-Boostrap](https://github.com/Maran23/svg-bootstrap)
- [SVG-FontAwesome](https://github.com/Maran23/svg-fontawesome)

## Requirements

| Dependency | Version  |
|------------|----------|
| Java       | 25+      |
| JavaFX     | 25+      |

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
SvgNode defaultIcon = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");

// Icon with a size and color
SvgNode icon32 = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", 32);
icon32.setSvgColor(Color.RED);

// Binding
SvgNode dynamic = new SvgNode();
dynamic.pathProperty().bind(viewModel.iconPathProperty());
dynamic.sizeProperty().bind(slider.valueProperty());
```

### FXML

```xml
<?import tools.maran.svgnode.SvgNode?>

<SvgNode path="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" size="32" svgColor="RED" />
```

#### Use String or Enum constants from an Icon Class:

```xml
<?import tools.maran.svgnode.SvgNode?>
<?import mypackage.MyIcons?>

<SvgNode size="32" svgColor="RED">
    <path>
        <MyIcons fx:constant="HOME"/>
    </path>
</SvgNode>
```

### Use with SVG Libraries

`SvgNode` works with any library that provides SVGs as a path.

```java
import tools.maran.svgnode.SvgNode;
import tools.maran.svg.bootstrap.Bootstrap;
import tools.maran.svg.fontawesome.FASolid;
import tools.maran.svg.materialdesign.MDIInterface;

SvgNode iconMDI = new SvgNode(MDIInterface.HOME.path());
SvgNode iconBootstrap = new SvgNode(Bootstrap.HOUSE.path());
SvgNode iconFA = new SvgNode(FASolid.HOME.path());
```

```xml
<?import tools.maran.svgnode.SvgNode?>
<?import tools.maran.svg.bootstrap.Bootstrap?>
<?import tools.maran.svg.fontawesome.FASolid?>
<?import tools.maran.svg.materialdesign.MDIInterface?>

<SvgNode>
    <path>
        <MDIInterface fx:constant="HOME"/>
    </path>
</SvgNode>
<SvgNode>
    <path>
        <Bootstrap fx:constant="HOUSE"/>
    </path>
</SvgNode>
<SvgNode>
    <path>
        <FASolid fx:constant="HOME"/>
    </path>
</SvgNode>
```

## Sampler

The sampler is in the tests and can be used to show examples and manually test the `SvgNode`.

Launch the `tools.maran.svgnode.manual.Sampler` class.

The sampler respects the light and dark color scheme of your OS, so you can see how it looks in both.

There are two categories in the sampler.

### SvgNode sampler

Shows the functionality of `SvgNode`.

- Using a `SvgNode` as `Label` and `Button` graphic, showing the automatic color switching based on the text color
- Changing the path, color, and size of the `SvgNode`

### SVG library explorer

Shows all SVGs in a grid from supported SVG libraries, ready to be explored.

Contains all SVG libraries mentioned [above](#svg-libraries)
