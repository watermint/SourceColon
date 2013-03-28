# SourceColon (Source: )

![Source:](https://raw.github.com/watermint/SourceColon/master/doc/icon-64.png)

Source code search based upon [OpenGrok](http://hub.opensolaris.org/bin/view/Project+opengrok/).

[![Build Status](https://travis-ci.org/watermint/SourceColon.png)](https://travis-ci.org/watermint/SourceColon.png)

## Objective

Provide simplified interface to search large scale source code tree.

## Requirements

* [Java SE 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Quick Start

Make sure your Java is later than Java SE 7 (1.7.0_xx or above)

    $ java -version
    java version "1.7.0_07"
	Java(TM) SE Runtime Environment (build 1.7.0_07-b10)
	Java HotSpot(TM) 64-Bit Server VM (build 23.3-b01, mixed mode)
	  
Run SourceColon

    $ ./gradlew -Ptarget=<your source code directory>

Open http://localhost:8080/ with your browser.

## License

CDDL 1.0. Please refer LICENSE.txt for more details.

