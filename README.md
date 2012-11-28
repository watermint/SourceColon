# SourceColon (Source: )

Source code search based upon [OpenGrok](http://hub.opensolaris.org/bin/view/Project+opengrok/).

[![Build Status](https://travis-ci.org/watermint/SourceColon.png)](https://travis-ci.org/watermint/SourceColon.png)

## Objective

Provide simplified interface to search large scale source code tree.

## Requirements

* [Java SE 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Gradle](http://www.gradle.org/)

## Quick Start

Make sure your Java is later than Java SE 7 (1.7.0_xx or above)

    $ java -version
    java version "1.7.0_07"
	Java(TM) SE Runtime Environment (build 1.7.0_07-b10)
	Java HotSpot(TM) 64-Bit Server VM (build 23.3-b01, mixed mode)
	  
Make sure you also have gradle

	$ gradle -v
	
	------------------------------------------------------------
	Gradle 1.3
	------------------------------------------------------------
	
	Gradle build time: 2012年11月20日 11時37分38秒 UTC
	Groovy: 1.8.6
	Ant: Apache Ant(TM) version 1.8.4 compiled on May 22 2012
	Ivy: 2.2.0
	JVM: 1.7.0_07 (Oracle Corporation 23.3-b01)
	OS: Mac OS X 10.7.5 x86_64

Run SourceColon

    $ gradle run -Ptarget=<your source code directory>
    :prepare
	:jflex
	:compileJava UP-TO-DATE
	:processResources UP-TO-DATE
	:classes UP-TO-DATE
	:jar UP-TO-DATE
	:runIndexer
	:prepareWar
	:war UP-TO-DATE
	> Building > :jettyRunWar > Running at http://localhost:8080/SourceColon

Open http://localhost:8080/SourceColon with your browser.

## License

CDDL 1.0. Please refer LICENSE.txt for more details.