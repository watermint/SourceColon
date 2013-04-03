/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.search;

import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.RuntimeEnvironment;
import org.watermint.sourcecolon.org.opensolaris.opengrok.util.GetOpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Search and list the matching files
 */
@SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.SystemPrintln"})
final class Search {

    private static final String usage = "USAGE: Search -R <configuration.xml> [-d | -r | -p | -h | -f] 'query string' ..\n" +
            "\t -R <configuration.xml> Read configuration from the specified file\n" +
            "\t -d Symbol Definitions\n" +
            "\t -r Symbol References\n" +
            "\t -p Path\n" +
            "\t -h History\n" +
            "\t -f Full text";

    private SearchEngine engine;
    private final List<Hit> results = new ArrayList<>();
    private int totalResults = 0;
    private int nhits = 0;

    @SuppressWarnings({"PMD.SwitchStmtsShouldHaveDefault"})
    protected boolean parseCmdLine(String[] argv) {
        engine = new SearchEngine();
        GetOpt getOpt = new GetOpt(argv, "R:d:r:p:h:f:");
        try {
            getOpt.parse();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(usage);
            return false;
        }

        int cmd;
        while ((cmd = getOpt.getOpt()) != -1) {
            switch (cmd) {
                case 'R':
                    try {
                        RuntimeEnvironment.getInstance().readConfiguration(new File(getOpt.getOptionArgument()));
                    } catch (Exception e) {
                        System.err.println("Failed to read config file: ");
                        System.err.println(e.getMessage());
                        return false;
                    }
                    break;
                case 'd':
                    engine.setDefinition(getOpt.getOptionArgument());
                    break;
                case 'r':
                    engine.setSymbol(getOpt.getOptionArgument());
                    break;
                case 'p':
                    engine.setFile(getOpt.getOptionArgument());
                    break;
                case 'h':
                    engine.setHistory(getOpt.getOptionArgument());
                    break;
                case 'f':
                    engine.setFreetext(getOpt.getOptionArgument());
                    break;
            }
        }

        return true;
    }

    protected boolean search() {
        if (RuntimeEnvironment.getInstance().getDataRootPath() == null) {
            System.err.println("You must specify a configuration file");
            System.err.println(usage);
            return false;
        }

        if (engine == null || !engine.isValidQuery()) {
            System.err.println("You did not specify a valid query");
            System.err.println(usage);
            return false;
        }

        results.clear();
        nhits = engine.search();
        if (nhits > 0) {
            engine.results(0, nhits, results);
        }
        totalResults = engine.totalHits;

        return true;
    }

    protected void dumpResults() {
        if (results.isEmpty()) {
            System.err.println("Your search \"" + engine.getQuery() + "\" did not match any files.");
        } else {
            String root = RuntimeEnvironment.getInstance().getSourceRootPath();
            System.out.println("Printing results 1 - " + nhits + " of " + totalResults + " total matching documents collected.");
            for (Hit hit : results) {
                File file = new File(root, hit.getPath());
                System.out.println(file.getAbsolutePath() + ":" + hit.getLineno() + " [" + hit.getLine() + "]");
            }

            if (nhits < totalResults) {
                System.out.println("Printed results 1 - " + nhits + " of " + totalResults + " total matching documents collected.");
                System.out.println("Collect the rest (y/n) ?");
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
                    String line = in.readLine();
                    if (null == line || line.length() == 0 || line.charAt(0) == 'n') {
                        return;
                    }
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                engine.results(nhits, totalResults, results);
                for (Hit hit : results) {
                    File file = new File(root, hit.getPath());
                    System.out.println(file.getAbsolutePath() + ":" + hit.getLineno() + " [" + hit.getLine() + "]");
                }
            }
        }
    }

    /**
     * usage Search index "query" prunepath
     *
     * @param argv command line arguments
     */
    public static void main(String[] argv) {
        Search searcher = new Search();
        boolean success = false;

        if (searcher.parseCmdLine(argv) && searcher.search()) {
            success = true;
            searcher.dumpResults();
        }

        if (!success) {
            System.exit(1);
        }
    }
}
