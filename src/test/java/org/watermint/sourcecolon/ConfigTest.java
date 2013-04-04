package org.watermint.sourcecolon;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 *
 */
public class ConfigTest {
    public class ConfigMock extends Config {
        public ConfigMock(String homePath) {
            super(homePath);
        }
    }

    private ConfigMock config;

    @Before
    public void setUp() throws Exception {
        config = new ConfigMock("");
    }

    @After
    public void tearDown() throws Exception {
        File d = new File(config.getHomePath());
        FileUtils.forceDelete(d);
    }

    @Test
    public void testGetInstance() throws Exception {

    }

    @Test
    public void testGetServer() throws Exception {

    }
}
