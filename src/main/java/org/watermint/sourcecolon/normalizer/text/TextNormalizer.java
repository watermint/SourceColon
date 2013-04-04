package org.watermint.sourcecolon.normalizer.text;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.ibm.icu.text.Normalizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TextNormalizer {
    /**
     * @param is input stream.
     * @return normalized text list.
     * @throws FileNotFoundException
     */
    public List<String> normalize(InputStream is) throws FileNotFoundException {
        List<String> list = new ArrayList<>();
        CharsetDetector detector = new CharsetDetector();
        InputStreamReader reader;
        try {
            CharsetMatch match = detector.setText(is).detect();
            is.reset();
            reader = new InputStreamReader(is, match.getName());
        } catch (IOException e) {
            reader = new InputStreamReader(is);
        }

        BufferedReader br = new BufferedReader(reader);
        String line = null;

        while (true) {
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                line = Normalizer.normalize(line, Normalizer.NFKC);
                list.add(line);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        return list;
    }
}
