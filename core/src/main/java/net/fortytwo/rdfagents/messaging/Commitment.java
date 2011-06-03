package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * A commitment (or lack thereof) to answer a query or accept a subscription.
 * A query may either be answered immediately (in which case the response to the initiator consists of the query result),
 * or at some later point in time (in which case an "agree" message is first sent to the initiator, to be followed by
 * another message with the query result).
 * Subscription requests must always be met with either an "agree" or a "refuse" message, before any updates are sent.
 */
public class Commitment {

    public enum Decision {
        AGREE_WITH_CONFIRMATION,     // Query and Subscribe
        AGREE_WITHOUT_CONFIRMATION,  // Query only
        REFUSE                       // Query and Subscribe
    }

    private final Decision decision;
    private final ErrorExplanation explanation;

    public Commitment(final Decision decision,
                      final ErrorExplanation explanation) {
        this.decision = decision;
        this.explanation = explanation;
    }

    public Decision getDecision() {
        return decision;
    }

    public ErrorExplanation getExplanation() {
        return explanation;
    }
}
