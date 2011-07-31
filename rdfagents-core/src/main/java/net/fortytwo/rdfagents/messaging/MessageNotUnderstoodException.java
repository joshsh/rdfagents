package net.fortytwo.rdfagents.messaging;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MessageNotUnderstoodException extends Exception {
    public MessageNotUnderstoodException(final String message) {
        super(message);
    }
}
