package net.fortytwo.rdfagents.messaging;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 3:38 AM
 */
public class LocalFailure extends Exception {
    public LocalFailure(final Throwable cause) {
        super(cause);
    }

    public LocalFailure(final String message) {
        super(message);
    }
}
