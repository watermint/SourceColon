package org.watermint.sourcecolon.solr;

/**
 *
 */
public class ConfigRuntimeException extends RuntimeException {
    public ConfigRuntimeException() {
    }

    public ConfigRuntimeException(String message) {
        super(message);
    }

    public ConfigRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigRuntimeException(Throwable cause) {
        super(cause);
    }
}
