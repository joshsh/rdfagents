package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MessageRejectedException extends Exception {
    private final ErrorExplanation explanation;

    public MessageRejectedException(final ErrorExplanation explanation) {
        this.explanation = explanation;
    }

    public ErrorExplanation getExplanation() {
        return explanation;
    }
}
