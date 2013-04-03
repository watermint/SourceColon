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
 * Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.configuration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.plain.PlainXref;

import static org.junit.Assert.*;

/**
 * Test the RuntimeEnvironment class
 *
 * @author Trond Norbye
 */
public class RuntimeEnvironmentTest {
    private static File orgiginalConfig;

    public RuntimeEnvironmentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // preserve the original
        orgiginalConfig = File.createTempFile("config", ".xml");
        RuntimeEnvironment.getInstance().writeConfiguration(orgiginalConfig);

        // Create a default configuration
        Configuration config = new Configuration();
        RuntimeEnvironment.getInstance().setConfiguration(config);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // restore the configuration
        RuntimeEnvironment.getInstance().readConfiguration(orgiginalConfig);
        RuntimeEnvironment.getInstance().register();
        orgiginalConfig.delete();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDataRoot() throws IOException {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        File f = File.createTempFile("dataroot", null);
        String path = f.getCanonicalPath();
        assertTrue(f.delete());
        assertFalse(f.exists());
        instance.setDataRoot(path);
        // setDataRoot() used to create path if it didn't exist, but that
        // logic has been moved. Verify that it is so.
        assertTrue(f.exists());
        assertEquals(path, instance.getDataRootPath());
        assertEquals(path, instance.getDataRootFile().getCanonicalPath());
    }

    @Test
    public void testSourceRoot() throws IOException {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertNull(instance.getSourceRootFile());
        assertNull(instance.getSourceRootPath());
        File f = File.createTempFile("sourceroot", null);
        String path = f.getCanonicalPath();
        assertTrue(f.delete());
        instance.setSourceRoot(path);
        assertEquals(path, instance.getSourceRootPath());
        assertEquals(path, instance.getSourceRootFile().getCanonicalPath());
    }

    @Test
    public void testUrlPrefix() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertEquals("/s?", instance.getUrlPrefix());
        String prefix = "/sourcecolon/s?";
        instance.setUrlPrefix(prefix);
        assertEquals(prefix, instance.getUrlPrefix());
    }

    @Test
    public void testCtags() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertEquals("ctags", instance.getCtags());
        String path = "/usr/bin/ctags";
        instance.setCtags(path);
        assertEquals(path, instance.getCtags());
    }

    @Test
    public void testGenerateHtml() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertTrue(instance.isGenerateHtml());
        instance.setGenerateHtml(false);
        assertFalse(instance.isGenerateHtml());
    }

    @Test
    public void testCompressXref() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertTrue(instance.isCompressXref());
        instance.setCompressXref(false);
        assertFalse(instance.isCompressXref());
    }

    @Test
    public void testQuickContextScan() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertFalse(instance.isQuickContextScan());
        instance.setQuickContextScan(true);
        assertTrue(instance.isQuickContextScan());
        instance.setQuickContextScan(false);
        assertFalse(instance.isQuickContextScan());
    }

    @Test
    public void testIndexWordLimit() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertEquals(Integer.MAX_VALUE, instance.getIndexWordLimit());  //default is unlimited
        instance.setIndexWordLimit(100000);
        assertEquals(100000, instance.getIndexWordLimit());
    }

    @Test
    public void testVerbose() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertFalse(instance.isVerbose());
        instance.setVerbose(true);
        assertTrue(instance.isVerbose());
    }

    @Test
    public void testAllowLeadingWildcard() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertFalse(instance.isAllowLeadingWildcard());
        instance.setAllowLeadingWildcard(true);
        assertTrue(instance.isAllowLeadingWildcard());
    }

    @Test
    public void testIgnoredNames() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertNotNull(instance.getIgnoredNames());
        instance.setIgnoredNames(null);
        assertNull(instance.getIgnoredNames());
    }

    @Test
    public void testUsingLuceneLocking() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertFalse(instance.isUsingLuceneLocking());
        instance.setUsingLuceneLocking(true);
        assertTrue(instance.isUsingLuceneLocking());
    }

    @Test
    public void testIndexVersionedFilesOnly() {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        assertFalse(instance.isIndexVersionedFilesOnly());
        instance.setIndexVersionedFilesOnly(true);
        assertTrue(instance.isIndexVersionedFilesOnly());
    }

    @Test
    public void testConfigListenerThread() throws IOException {
        RuntimeEnvironment instance = RuntimeEnvironment.getInstance();
        SocketAddress addr = new InetSocketAddress(0);
        assertTrue(instance.startConfigurationListenerThread(addr));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exp) {
            // do nothing
        }
        instance.writeConfiguration();
        instance.stopConfigurationListenerThread();
    }

    @Test
    public void testObfuscateEMail() throws IOException {
        RuntimeEnvironment env = RuntimeEnvironment.getInstance();

        // By default, don't obfuscate.
        assertObfuscated(false, env);

        env.setObfuscatingEMailAddresses(true);
        assertObfuscated(true, env);

        env.setObfuscatingEMailAddresses(false);
        assertObfuscated(false, env);
    }

    private void assertObfuscated(boolean expected, RuntimeEnvironment env)
            throws IOException {
        assertEquals(expected, env.isObfuscatingEMailAddresses());

        String address = "opengrok-discuss@opensolaris.org";

        PlainXref xref = new PlainXref(new StringReader(address));
        StringWriter out = new StringWriter();
        xref.write(out);

        String expectedAddress = expected ?
                address.replace("@", " (at) ") : address;

        String expectedOutput =
                "<a name=\"1\"></a>"
                        + expectedAddress;

        assertEquals(expectedOutput, out.toString());
    }
}
