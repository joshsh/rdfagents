package net.fortytwo.rdfagents.jade.sesame;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 3:49 PM
 */
public class RDFWriteException extends Exception {
    public RDFWriteException(final Throwable cause) {
        super(cause);
    }

    public RDFWriteException(final String message) {
        super(message);
    }
}
