package net.fortytwo.rdfagents.messaging;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LocalFailure extends Exception {
    public LocalFailure(final Throwable cause) {
        super(cause);
    }

    public LocalFailure(final String message) {
        super(message);
    }
}
