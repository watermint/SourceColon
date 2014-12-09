# Source: (SourceColon)

![Source:](https://raw.github.com/watermint/SourceColon/master/doc/icon-64.png)

Source code search based upon [OpenGrok](http://opengrok.github.com/OpenGrok/).

[![Build Status](https://travis-ci.org/watermint/SourceColon.png)](https://travis-ci.org/watermint/SourceColon)

## Objective

Provide simplified interface to search large scale source code tree.

## Prerequisites

* [Java SE 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later.
* Optional: [Exuberant Ctags](http://ctags.sourceforge.net/) 
	* Excuberant Ctags provides tags for codes. Tags are used in definition search and navigation (class, function, variable index of each code).

## Quick Start

Make sure your Java is later than Java SE 7 (1.7.0_xx or above)

```sh
% java -version
java version "1.8.0_25"
Java(TM) SE Runtime Environment (build 1.8.0_25-b17)
Java HotSpot(TM) 64-Bit Server VM (build 25.25-b02, mixed mode)
```

Run SourceColon

    $ ./gradlew -Ptarget=<your source code directory>

Open http://localhost:8080/ with your browser.

## Usage

Search by full text, definition (excuberant ctags required), symbol, or path of the file. Enter your query on Search boxes. 

![Search](http://farm9.staticflickr.com/8260/8601224975_b7d1b25331_b.jpg)

Then browse your source codes. If the system have excuberant ctags on indexing, navigation to class, package, function/method, etc. are displayed on left pane.

![Browse](http://farm9.staticflickr.com/8402/8602344778_f300d570de_b.jpg)

## License

CDDL 1.0. Please refer LICENSE.txt for more details.

