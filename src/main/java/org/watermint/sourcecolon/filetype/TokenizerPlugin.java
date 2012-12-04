package org.watermint.sourcecolon.filetype;

import java.util.List;

/**
 * Tokenizer.
 */
public interface TokenizerPlugin {
    /**
     * MIME Type.
     * @return MIME Type for file type.
     */
    String mimeType();

    /**
     * Possible suffix for file type.
     * @return Suffix list (case insensitive).
     *         null when no specific suffix for the file type.
     */
    List<String> possibleSuffixes();

    /**
     * Possible magic number for file type.
     * @return Magic Number byte array.
     *         null when no specific magic number for the file type.
     */
    List<byte[]> possibleMagicNumbers();
}
