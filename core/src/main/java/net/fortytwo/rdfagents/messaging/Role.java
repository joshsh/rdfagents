package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.Agent;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 3:37 PM
 */
public abstract class Role {
    private final Agent agent;

    public Role(Agent agent) {
        this.agent = agent;
    }

    public Agent getAgent() {
        return agent;
    }
}
