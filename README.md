**_TocMe_** is a Gradle plugin which adds a Table of Contents to markdown documents and keeps it up to date.

This plugin uses the great and powerful [flexmark-java](https://github.com/vsch/flexmark-java).

# Table of Contents
<!-- toc -->
- __[Getting started](#getting-started)__
  - __[Add the plugin](#add-the-plugin)__
  - __[Prepare the markdown document](#prepare-the-markdown-document)__
  - __[Add the tocme specification](#add-the-tocme-specification)__
  - __[Run the insertTocs task](#run-the-inserttocs-task)__
- __[Notes](#notes)__
    - __[Multiple TOCs in one document](#multiple-tocs-in-one-document)__
    - __[Putting TOCs in multiple documents](#putting-tocs-in-multiple-documents)__
    - __[Backups](#backups)__
- __[Options](#options)__
  - __[In the markdown document itself](#in-the-markdown-document-itself)__
  - __[In Gradle](#in-gradle)__
    - __[Output files](#output-files)__
    - __[Specifying options](#specifying-options)__
    - __[Available options](#available-options)__
- __[Changelog](#changelog)__
  - __[1.3](#13)__
  - __[1.2](#12)__
  - __[1.1](#11)__
  - __[1.0](#10)__
<!-- /toc -->

# Getting started

## Add the plugin

Add the plugin to your project's `build.gradle`:

```groovy
plugins {
  id "com.github.blueboxware.tocme" version "1.3"
}
```

## Prepare the markdown document

Put the following "markers" in the markdown document at the location where the TOC should be placed:

```markdown
<!-- toc -->
<!-- /toc -->

```

:warning:  Don't remove these markers after the TOC is inserted: they are also used to keep the TOC up to date.

## Add the `tocme` specification

Add the `tocme` specification to `build.gradle`. For example, if you want a TOC in the file `README.md`:

```gradle
tocme {

    doc(file("README.md"))

}
```

## Run the `insertTocs` task

Run the `insertTocs` Gradle task.

This will insert the TOC in the specified document. When you run the `insertTocs` task again after changing the document, it will update the
TOC if necessary (to make updating possible, you should not remove the `<!-- toc -->` and `<!-- /toc -->` markers from the document after
the TOC is inserted).

To only check if the TOCs are up to date, without making any changes, run the `checkTocs` task.

# Notes

### Multiple TOCs in one document
You can put multiple sets of markers in one document. A TOC will be inserted at each of the specified locations. By default each TOC will
only contain the headers appearing _after_ the TOC.

You can use the `mode` option (see below) to specify that a TOC should also include headers appearing above the TOC, or that a
"local" TOC should be created (a local TOC only includes the subheaders of the header under which the TOC marker is placed).

### Putting TOCs in multiple documents
Repeat the `doc` directive and/or use `docs`:

```gradle
tocme {

    doc("README.md")
    doc(file("doc/reference.md"))

    docs("doc/intro.md", "doc/notes.md")

}
```

### Backups
Backups of all files which are changed or overwritten by the `insertTocs` task are created in `build/tocme/backups/` before making changes.

# Options

## In the markdown document itself
You can put a number of space-separated options in the opening markers in a document:

```markdown
<!-- toc mode=full style=flat levels=1-4 -->
<!-- /toc -->

```

The following options are available:

| Name  | Default | Description |
| ------------- | ------------- | ------------- |
| **`style`**  | `hierarchy`  | The style to use for the TOC. Possible values: `hierarchy`, `flat`, `reversed` (flat in reversed order), `increasing` (flat, alphabetically sorted) or `decreasing` (flat, alphabetically reversed sorted). |
| **`mode`**   | `normal`     | Determines which headers to include. Possible values: `normal` (only headers which appear after the TOC), `full` (include headers appearing before the TOC) or `local` (only subheaders of the header above the marker). |
| **`levels`** | `1-3`          | Which header levels to include. Examples of possible values: "`1-4`", "`1,2,3,4`" (same), "`1-2,3-4`" (same). |
| **`numbered`** | `false`      | When true: number the headers in the TOC. |
| **`bold`**   | `true`         | When true: render the headers in the TOC bold. |
| **`plain`**  | `false`        | When true: don't make the headers links to their respective sections. |

## In Gradle

### Output files

Instead of having the TOC put in the input file, you can specify one or more output files. The new documents, with the TOC(s), are written to the
specified output files, leaving the input file unchanged:

```gradle
tocme {

    doc("notes.in.md") {
        output("notes.md")
    }

    doc("README.md") {
        outputs("readme_with_toc.md", "doc/readme.md")
    }

}
```

### Specifying options

In the `tocme` specification, default options can be specified for all documents:

```gradle
tocme {

    bold = false
    variant = Kramdown

    doc("README.md")
    doc("notes.md")

}
```

or for a specific input document:

```gradle
tocme {

    doc("README.md")

    doc("notes.md") {
        numbered = true
        style = Flat

        output("notes_with_toc.md")
    }

}
```

or for a specific output file:

```gradle
tocme {

    doc("notes.src.md") {

        output("notes.md")

        output("doc/notes.md") {
            levels = levels("1-5")
            mode = Local
        }

    }

}
```

When the same option is specified multiple times with different values, the value specified in the `<!-- toc -->` marker takes preference,
next the one specified for the output file, followed by the the value specified for the input file and lastly the value specified as default
for the `tocme` specification.

### Available options
The following options can be used in `build.gradle`:

| Name  | Type | Default | Description |
| ------------- | ----- | ------------- | ------------- |
| **`variant`** | <kbd>enum</kbd> | `GitHub` | The markdown variant to use when parsing the document and creating the TOC. Possible values: `Commonmark`, `Commonmark26` (Commonmark v0.26), `Commonmark27` (v0.27), `Commonmark28` (v0.28), `Kramdown`, `Markdown`, `GitHub`, `GitHubDoc`, `GitLab`, `MultiMarkdown`, `Pegdown` and `PegdownStrict`. |
| **`style`**  | <kbd>enum</kbd> | `Hierarchy`  | The style to use for the TOC. Possible values: `Hierarchy`, `Flat`, `Reversed` (flat in reversed order), `Increasing` (flat, alphabetically sorted) or `Decreasing` (flat, alphabetically reversed sorted). |
| **`mode`**   | <kbd>enum</kbd> | `Normal`     | Determines which headers to include. Possible values: `Normal` (only headers which appear after the TOC), `Full` (include headers appearing before the TOC) or `Local` (only subheaders of the header above the marker). |
| **`levels`** | <kbd>int</kbd> | `[1, 2, 3]` (levels 1-3) | A collection of integers specifying the level numbers to include. Use the `levels()` function to specify a string instead, for example: `levels = levels("1,3-5")` |
| **`numbered`** | <kbd>bool</kbd> |`false`      | When true: number the headers in the TOC. |
| **`bold`**   | <kbd>bool</kbd> | `true`         | When true: render the headers in the TOC bold. |
| **`plain`**  | <kbd>bool</kbd> | `false`        | When true: don't make the headers links to their respective sections. |

#### Advanced options

| Name  | Type | Default | Description |
| ------| ---- | ------- | ------------- |
| **`tag`** | <kbd>string</kbd> | `"toc"` | The keyword to use in the toc-markers in the markdown document. |
| **`removeEmojis`** | <kbd>bool</kbd> | `false` | Remove emojis from the header texts in the TOC. |
| **`requireSpace`** | <kbd>bool</kbd> | `true` | When true: don't recognize headers without a space between the # and the header text (`#Header`) |
| **`dupedDashes`** | <kbd>bool</kbd> | ...<sup>1</sup> | When false: replace duplicate dashes in header link ids with a single dash. |
| **`resolveDupes`** | <kbd>bool</kbd> | ...<sup>1</sup> | When true: add a number to duplicate header link ids to make them unique. |
| **`dashChars`** | <kbd>string</kbd> | ...<sup>1</sup> | A string specifying the characters to replace with a dash in header link ids.<sup>2</sup> |
| **`allowedChars`** | <kbd>string</kbd> | ...<sup>1</sup> | A string specifying the characters which are allowed in header link ids. Alphanumeric characters are always allowed.<sup>2</sup>  |
| **`allowLeadingSpace`** | <kbd>bool</kbd> | ...<sup>1</sup> | When true: allow non-indent spaces before headers. |
| **`emptyHeadingWithoutSpace`** | <kbd>bool</kbd> | `true` | When false: don't recognize empty headers without a space following the '#'. |
| **`setextMarkerLength`** | <kbd>int</kbd> | ...<sup>1</sup> | The minimum number of `-` or `=` characters to use under a setext header for it to be recognized as header. |
| **`headingInterruptsItemParagraph`** | <kbd>bool</kbd> | `true` | When true: allow headings to interrupt list item paragraphs. |

<sup>1</sup>: Default depends on the used `variant`.

<sup>2</sup>: Non alphanumeric characters are removed from header links ids, except for the characters specified
in `dashChars`, which are replaced by a dash, and characters in `allowedChars`, which are not removed or replaced but left as is.

# Changelog

## 1.3
* Compatibility with Gradle 7.1 ([#2](https://github.com/BlueBoxWare/TocMe/issues/2))

## 1.2
* Changed the way the included levels are specified in Gradle. The `levels` parameter now takes
a collection of integers.
* Update to Flexmark 0.62.2
* Update to Gradle 6.5.1

## 1.1
* `checkTocs` now fails the build if there are out of date TOCS.

## 1.0
* Initial version