package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.RDFAgent;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class Role {
    protected final RDFAgent agent;

    public Role(final RDFAgent agent) {
        this.agent = agent;
    }
}
