package net.fortytwo.rdfagents.model;

import org.openrdf.model.URI;

import java.util.Collection;

/**
 * User: josh
 * Date: 2/24/11
 * Time: 7:32 PM
 */
public class AgentReference {
    private final URI name;
    private final URI[] transportAddresses;

    public AgentReference(final URI name,
                          final URI[] transportAddresses) {
        this.name = name;
        this.transportAddresses = transportAddresses;
    }

    public URI getName() {
        return name;
    }

    public URI[] getTransportAddresses() {
        return transportAddresses;
    }
}
