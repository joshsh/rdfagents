package net.fortytwo.rdfagents.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class AgentId {
    private final URI name;
    private final URI[] transportAddresses;

    public AgentId(final String name,
                   final String... transportAddresses) {
        this.name = new URIImpl(name);
        this.transportAddresses = new URI[transportAddresses.length];
        for (int i = 0; i < transportAddresses.length; i++) {
            this.transportAddresses[i] = new URIImpl(transportAddresses[i]);
        }
    }

    public AgentId(final URI name,
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
