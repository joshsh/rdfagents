package net.fortytwo.rdfagents.messaging.push;

import net.fortytwo.rdfagents.model.Agent;

/**
 * ...a one-way messaging pattern in which the sender does not require a response...
 * ...does not use a specific FIPA protocol...
 *
 * User: josh
 * Date: 2/22/11
 * Time: 5:03 PM
 *
 * @param <N> a class of notifications
 */
public abstract class Notification<N> {
    private final Agent sender;
    private final Agent recipient;
    private final N content;

    public Notification(final Agent sender,
                        final Agent recipient,
                        final N content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    public Agent getSender() {
        return sender;
    }

    public Agent getRecipient() {
        return recipient;
    }

    public N getContent() {
        return content;
    }

    public abstract void send();
}
