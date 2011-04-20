package net.fortytwo.rdfagents.model;

/**
 * User: josh
 * Date: 2/22/11
 * Time: 4:50 PM
 */
public abstract class Agent {
    private final AgentReference id;

    public Agent(final AgentReference id) {
        this.id = id;
    }

    public AgentReference getId() {
        return id;
    }
}
