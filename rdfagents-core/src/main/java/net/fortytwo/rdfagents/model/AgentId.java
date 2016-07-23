package net.fortytwo.rdfagents.model;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class AgentId {
    private final String name;
    private final IRI iri;
    private final IRI[] transportAddresses;

    public AgentId(final String name,
                   final String... transportAddresses) {
        this.name = name;

        this.iri = iriFromName(name);

        this.transportAddresses = new IRI[transportAddresses.length];
        for (int i = 0; i < transportAddresses.length; i++) {
            this.transportAddresses[i] = SimpleValueFactory.getInstance().createIRI(transportAddresses[i]);
        }
    }

    public AgentId(final IRI iri,
                   final IRI[] transportAddresses) {
        this.iri = iri;
        this.name = nameFromIri(iri);
        this.transportAddresses = transportAddresses;
    }

    public String getName() {
        return name;
    }

    public IRI getIri() {
        return iri;
    }

    public IRI[] getTransportAddresses() {
        return transportAddresses;
    }

    private static IRI iriFromName(String name) {
        // attempt to make the name into an IRI if it isn't one already.
        // It is not always possible to pass a valid IRI as an agent identifier.
        String iriStr = name;
        if (!iriStr.startsWith("http://") && !iriStr.startsWith("urn:")) {
            iriStr = "urn:" + iriStr;
        }

        return SimpleValueFactory.getInstance().createIRI(iriStr);
    }

    private static String nameFromIri(final IRI iri) {
        String s = iri.stringValue();
        return s.startsWith("http://") ? s.substring(7) : s.startsWith("urn:") ? s.substring(4) : s;
    }
}
