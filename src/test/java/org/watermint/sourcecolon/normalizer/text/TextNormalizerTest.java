package org.watermint.sourcecolon.normalizer.text;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class TextNormalizerTest {
    private Map<List<String>, String> createNormalizeTests() {
        Map<List<String>, String> tests = new HashMap<>();

        // LF
        tests.put(Arrays.asList("SourceColon#1", "SourceColon#2"), "SourceColon#1\nSourceColon#2");
        // LF + CR
        tests.put(Arrays.asList("SourceColon#1", "SourceColon#2"), "SourceColon#1\n\rSourceColon#2");
        // CR + LF
        tests.put(Arrays.asList("SourceColon#1", "SourceColon#2"), "SourceColon#1\r\nSourceColon#2");
        // CR
        tests.put(Arrays.asList("SourceColon#1", "SourceColon#2"), "SourceColon#1\rSourceColon#2");
        // LF + LF
        tests.put(Arrays.asList("SourceColon#1", "", "SourceColon#2"), "SourceColon#1\n\nSourceColon#2");
        // LF at first
        tests.put(Arrays.asList("", "SourceColon#1", "SourceColon#2"), "\nSourceColon#1\nSourceColon#2");
        // LF at last
        tests.put(Arrays.asList("SourceColon#1", "SourceColon#2", ""), "SourceColon#1\nSourceColon#2\n");


        return tests;
    }

    @Test
    public void testNormalize() throws Exception {
        TextNormalizer tn = new TextNormalizer();

        for (Map.Entry<List<String>, String> e : createNormalizeTests().entrySet()) {
            assertEquals(e.getKey(), tn.normalize(new ByteArrayInputStream(e.getValue().getBytes())));
        }
    }
}
