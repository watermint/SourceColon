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
 * Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.util;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit test for org.watermint.sourcecolon.org.opensolaris.opengrok.util.GetOpts
 */
public class GetOptsTest {

    public GetOptsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParseNormal() throws Exception {
        String[] argv = new String[]{"-a", "foo", "-bc", "--", "-f"};
        GetOpts instance = new GetOpts(argv, "a:bcr:f");

        instance.parse();

        assertEquals('a', (char) instance.getOpt());
        assertEquals("foo", instance.getOptionArgument());
        assertEquals('b', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals('c', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals(-1, instance.getOpt());
        assertEquals(4, instance.getOptionIndex());
        assertTrue(instance.getOptionIndex() < argv.length);
        assertEquals("-f", argv[instance.getOptionIndex()]);
    }

    @Test
    public void reset() throws ParseException {
        String[] argv = new String[]{"-a", "foo", "-bc", "argument1"};
        GetOpts instance = new GetOpts(argv, "a:bc");

        instance.parse();

        assertEquals('a', (char) instance.getOpt());
        assertEquals("foo", instance.getOptionArgument());
        assertEquals('b', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals('c', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals(-1, instance.getOpt());
        assertEquals(3, instance.getOptionIndex());
        assertTrue(instance.getOptionIndex() < argv.length);
        assertEquals("argument1", argv[instance.getOptionIndex()]);

        instance.reset();

        assertEquals('a', (char) instance.getOpt());
        assertEquals("foo", instance.getOptionArgument());
        assertEquals('b', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals('c', (char) instance.getOpt());
        assertNull(instance.getOptionArgument());
        assertEquals(-1, instance.getOpt());
        assertEquals(3, instance.getOptionIndex());
        assertTrue(instance.getOptionIndex() < argv.length);
        assertEquals("argument1", argv[instance.getOptionIndex()]);
    } /* Test of reset method, of class GetOpts. */

    @Test
    public void testParseFailure() throws Exception {
        String[] argv = new String[]{"-a"};
        GetOpts instance = new GetOpts(argv, "a:");

        try {
            instance.parse();
            fail("Parse shall not allow missing arguments");
        } catch (ParseException exp) {
            if (!exp.getMessage().contains("requires an argument")) {
                // not the exception we expected
                throw exp;
            }
        }

        instance = new GetOpts(argv, "b");
        try {
            instance.parse();
            fail("Parse shall not allow unknown arguments");
        } catch (ParseException exp) {
            if (!exp.getMessage().contains("Unknown argument: ")) {
                // not the exception we expected
                throw exp;
            }
        }
    }
}
