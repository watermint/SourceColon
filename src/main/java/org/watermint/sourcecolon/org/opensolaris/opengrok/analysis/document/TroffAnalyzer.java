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
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.document;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.Definitions;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzerFactory;
import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.Project;

import java.io.*;
import java.util.Arrays;

/**
 * Analyzes [tn]roff files
 * Created on September 30, 2005
 *
 * @author Chandan
 */
public final class TroffAnalyzer extends FileAnalyzer {
    private char[] content;
    private int len;

    private final TroffFullTokenizer troffull;
    private final TroffXref xref;

    /**
     * Creates a new instance of TroffAnalyzer
     */
    protected TroffAnalyzer(FileAnalyzerFactory factory) {
        super(factory);
        Reader dummy = new StringReader("");
        troffull = new TroffFullTokenizer(dummy);
        xref = new TroffXref(dummy);
        content = new char[12 * 1024];
    }

    @Override
    public void analyze(Document doc, InputStream in) throws IOException {
        len = 0;
        do {
            InputStreamReader inReader = new InputStreamReader(in);
            int rbytes = inReader.read(content, len, content.length - len);
            if (rbytes > 0) {
                if (rbytes == (content.length - len)) {
                    content = Arrays.copyOf(content, content.length * 2);
                }
                len += rbytes;
            } else {
                break;
            }
        } while (true);

        doc.add(new Field("full", new StringReader("")));
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        if ("full".equals(fieldName)) {
            troffull.reInit(content, len);
            return troffull;
        }
        return super.tokenStream(fieldName, reader);
    }

    /**
     * Write a cross referenced HTML file.
     *
     * @param out Writer to write HTML cross-reference
     */
    @Override
    public void writeXref(Writer out) throws IOException {
        xref.reInit(content, len);
        xref.project = project;
        out.write("</pre><div id=\"man\">");
        xref.write(out);
        out.write("</div><pre>");
    }

    /**
     * Write a cross referenced HTML file reads the source from in
     *
     * @param in         Input source
     * @param out        Output xref writer
     * @param defs       definitions for the file (could be null)
     */
    static void writeXref(Reader in, Writer out, Definitions defs, Project project) throws IOException {
        TroffXref xref = new TroffXref(in);
        xref.project = project;
        xref.setDefs(defs);
        out.write("</pre><div id=\"man\">");
        xref.write(out);
        out.write("</div><pre>");
    }
}
