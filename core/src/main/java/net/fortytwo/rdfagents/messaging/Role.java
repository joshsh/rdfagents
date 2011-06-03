package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.RDFAgent;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 3:37 PM
 */
public abstract class Role {
    protected final RDFAgent agent;

    public Role(final RDFAgent agent) {
        this.agent = agent;
    }
}
