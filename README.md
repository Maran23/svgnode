# SvgNode project

A lightweight, optimized JavaFX node for rendering SVG paths at any size. Fully supports FXML, property binding, and CSS styling.

![Sampler](https://github.com/Maran23/svgnode/blob/demo/demo/sampler.webp)

<details>
<summary>SVG Libraries</summary>

## Sampler with SVG Libraries

### Material Design
![Material Design](https://github.com/Maran23/svgnode/blob/demo/demo/library_materialdesign.webp)

### Bootstrap
![Bootstrap](https://github.com/Maran23/svgnode/blob/demo/demo/library_bootstrap.webp)

### Font Awesome
![FontAwesome](https://github.com/Maran23/svgnode/blob/demo/demo/library_fontawesome.webp)

</details>

## Table of Contents

- [Features](#features)
- [Installation](#installation)
    - [Maven](#maven)
    - [Gradle](#gradle)
- [Usage](#usage)
    - [Java](#java)
    - [FXML](#fxml)
    - [Use with SVG Libraries](#use-with-svg-libraries)
- [Sampler](#sampler)
- [API / Motivation](#api-and-motivation)

---

## Features

- 🎨 Render any SVG path as a JavaFX node
- 🔗 No dependencies - will use your provided JavaFX runtime
- ⚡ Optimized to be efficient and have a tiny footprint, so you can render 100 `SvgNode` without any problem. 
  - This is achieved by `SvgNode` extending from `Parent`, skipping size calculations and only initializing properties when needed
- 📐 Uniform rasterization with a single `size` property
- 📄 FXML-compatible with attribute and constant-based usage
- 🎭 CSS-stylable via `.svg-node` and `.svg` style classes. By default, the SVG automatically adjusts its color based on the background - just like text

### With SVG Libraries

Will work fine with SVG Libraries (see usage [below](#use-with-svg-libraries)) such as:
- [SVG-MaterialDesign](https://github.com/Maran23/svg-materialdesign)
- [SVG-Boostrap](https://github.com/Maran23/svg-bootstrap)
- [SVG-FontAwesome](https://github.com/Maran23/svg-fontawesome)

## Installation

### Requirements

| Dependency | Version  |
|------------|----------|
| Java       | 25+      |
| JavaFX     | 25+      |

### Maven

```xml
<dependency>
    <groupId>tools.maran</groupId>
    <artifactId>svgnode</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'tools.maran:svgnode:2.0.0'
```

## Usage

### Java

```java
import tools.maran.svgnode.SvgNode;

// Default size (24px)
SvgNode defaultIcon = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");

// Icon with a size and color
SvgNode icon32 = new SvgNode("M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", 32);
icon32.setColor(Color.RED);

// Binding
SvgNode dynamic = new SvgNode();
dynamic.pathProperty().bind(viewModel.iconPathProperty());
dynamic.sizeProperty().bind(slider.valueProperty());
```

### FXML

```xml
<?import tools.maran.svgnode.SvgNode?>

<SvgNode path="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" size="32" color="RED" />
```

#### Use String or Enum constants from an Icon Class:

```xml
<?import tools.maran.svgnode.SvgNode?>
<?import mypackage.MyIcons?>

<SvgNode size="32" color="RED">
    <path>
        <MyIcons fx:constant="HOME"/>
    </path>
</SvgNode>
```

### Use with SVG Libraries

`SvgNode` works with any library that provides SVGs as a path. Below are some examples where the SVGs are located in an Enum.

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

Launch the following class located in the tests:
```shell
tools.maran.svgnode.manual.Sampler
```

The sampler respects the light and dark color scheme of your OS, so you can see how it looks in both.
There are two categories in the sampler.

### SvgNode sampler

Shows the functionality of `SvgNode`.

- Using a `SvgNode` as `Label` and `Button` graphic, showing the automatic color switching based on the text color
- Changing the path, color, and size of the `SvgNode`

### SVG library explorer

Shows all SVGs in a grid from supported SVG libraries, ready to be explored.

Contains all SVG libraries mentioned [above](#svg-libraries).

## API and Motivation

### Motivation

For a long time, the most common way to show icons in JavaFX was done by using Icon fonts. This works, but is not perfect, e.g. sometimes Icons are blurry or the size is not what I would expect. It is also tricky to get theming right, especially when supporting the light and dark color scheme.

`SvgNode` aims to take that approach to a new level: Showing SVGs instead.

While there is the `SvgPath` in JavaFX that does the heavy lifting of parsing the SVG path, rendering it pixel perfect is actually not that straight forward. 
In fact, even in JavaFX the SVGs (e.g. arrow) that are used rely on some weird CSS and sizing to get that done right. For example, the arrow in the TitledPane required a Region in a Region and a hardcoded padding.

I think we can do better, and `SvgNode` encapsulates all that logic for you. It is always pixel perfect, works with most SVGs libraries out there and can easily change the color.

### API

Details about the API naming and changelog.

#### Naming

For Web-Applications, there are many SVG libraries for all the different frameworks and SVG icon sets.
They all have the following in common:
- There is either a `icon=` property or you can use the SVG directly, like `<Home />` 
- There is a `size` property (default is often 24)
- There is `color` property

So what you have is usually this:
```html
<Home size={48} color="red" />

<SvgIcon icon={home} size={48} color="red"/>
```

The API of `SvgNode` matches what is the de-facto standard for SVG libraries, in a way that is compatible with JavaFX.

#### Changelog

> Version 2.0.0

- Made all properties CSS styleable. This is especially interesting together with CSS transitions.
- Renamed `svgColor` to `color`. This is a breaking change. See the naming above why this was done.

> Version 1.0.0

- Initial release
