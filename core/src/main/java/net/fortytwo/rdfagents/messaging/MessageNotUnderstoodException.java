package net.fortytwo.rdfagents.messaging;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 4:16 AM
 */
public class MessageNotUnderstoodException extends Exception {
    public MessageNotUnderstoodException(final String message) {
        super(message);
    }
}
