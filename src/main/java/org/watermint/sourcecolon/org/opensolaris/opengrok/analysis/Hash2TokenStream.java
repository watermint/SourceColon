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
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.Iterator;
import java.util.Set;

public final class Hash2TokenStream extends TokenStream {
    private int i = 0;
    private String[] terms;
    private Iterator<String> keys;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public Hash2TokenStream(Set<String> symbols) {
        keys = symbols.iterator();
    }

    @Override
    public boolean incrementToken() throws java.io.IOException {
        while (i <= 0) {
            if (keys.hasNext()) {
                String term = keys.next();
                terms = term.split("[^a-zA-Z_0-9]+");
                i = terms.length;
                if (i > 0) {
                    termAtt.setEmpty();
                    termAtt.append(terms[--i]);
                    return true;
                }
                // no tokens found in this key, try next
                continue;
            }
            return false;
        }

        termAtt.setEmpty();
        termAtt.append(terms[--i]);
        return true;
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
