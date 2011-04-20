package net.fortytwo.rdfagents.messaging.push;

import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.Agent;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 2:46 PM
 */
public abstract class Receiver<N> extends Role {
    public Receiver(Agent agent) {
        super(agent);
    }

    public abstract void receive(Notification<N> note);
}
