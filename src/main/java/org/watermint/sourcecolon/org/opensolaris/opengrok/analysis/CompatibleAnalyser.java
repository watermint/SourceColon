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
 * Copyright 2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.plain.PlainFullTokenizer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.plain.PlainSymbolTokenizer;

import java.io.Reader;

public final class CompatibleAnalyser extends Analyzer {
    private PathAnalyzer pathAnalyzer;

    public CompatibleAnalyser() {
        pathAnalyzer = new PathAnalyzer();
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        switch (fieldName) {
            case "full":
                return new PlainFullTokenizer(reader);
            case "refs":
                return new PlainSymbolTokenizer(reader);
            case "defs":
                return new PlainSymbolTokenizer(reader);
            case "path":
            case "project":
                return pathAnalyzer.tokenStream(fieldName, reader);
        }
        return new PlainFullTokenizer(reader);
    }
}
