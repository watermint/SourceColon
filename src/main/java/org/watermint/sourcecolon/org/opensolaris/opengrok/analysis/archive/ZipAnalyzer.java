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
package org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.archive;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzerFactory;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.plain.PlainFullTokenizer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.web.Util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Analyzes Zip files
 * Created on September 22, 2005
 *
 * @author Chandan
 */
public final class ZipAnalyzer extends FileAnalyzer {
    private final StringBuilder content;

    private static final Reader dummy = new StringReader("");

    private final PlainFullTokenizer plainfull;

    protected ZipAnalyzer(FileAnalyzerFactory factory) {
        super(factory);
        content = new StringBuilder(64 * 1024);
        plainfull = new PlainFullTokenizer(dummy);
    }

    @Override
    public void analyze(Document doc, InputStream in) throws IOException {
        content.setLength(0);
        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            content.append(entry.getName()).append('\n');
        }
        content.trimToSize();
        doc.add(new Field("full", dummy));
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        if ("full".equals(fieldName)) {
            char[] cs = new char[content.length()];
            content.getChars(0, cs.length, cs, 0);
            plainfull.reInit(cs, cs.length);
            return plainfull;
        }
        return super.tokenStream(fieldName, reader);
    }

    /**
     * Write a cross referenced HTML file.
     *
     * @param out Writer to store HTML cross-reference
     */
    @Override
    public void writeXref(Writer out) throws IOException {
        out.write(Util.htmlize(content));
    }
}
