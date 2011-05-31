package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * User: josh
 * Date: 5/24/11
 * Time: 4:13 PM
 */
public class FailureException extends Exception {
    private final ErrorExplanation explanation;

    public FailureException(final Throwable cause) {
        super(cause);
        explanation = null;
    }

    public FailureException(final String message) {
        super(message);
        explanation = null;
    }

    public FailureException(final ErrorExplanation explanation) {
        this.explanation = explanation;
    }

    public ErrorExplanation getExplanation() {
        return explanation;
    }
}
