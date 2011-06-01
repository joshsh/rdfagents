package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * User: josh
 * Date: 5/24/11
 * Time: 4:13 PM
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
