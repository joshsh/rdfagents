package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.AgentReference;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 3:37 PM
 */
public abstract class Role {
    private final AgentReference agent;

    public Role(AgentReference agent) {
        this.agent = agent;
    }

    public AgentReference getAgent() {
        return agent;
    }
}
