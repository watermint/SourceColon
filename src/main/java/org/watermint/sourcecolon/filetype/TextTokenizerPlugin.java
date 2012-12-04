package org.watermint.sourcecolon.filetype;

import java.util.List;

/**
 *
 */
public interface TextTokenizerPlugin extends TokenizerPlugin {
    /**
     * Reserved keywords.
     * @return Reserved keyword list for file type.
     */
    List<String> reservedKeywords();
}
