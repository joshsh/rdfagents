package net.fortytwo.rdfagents.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class AgentId {
    private final String name;
    private final URI uri;
    private final URI[] transportAddresses;

    public AgentId(final String name,
                   final String... transportAddresses) {
        this.name = name;

        this.uri = uriFromName(name);

        this.transportAddresses = new URI[transportAddresses.length];
        for (int i = 0; i < transportAddresses.length; i++) {
            this.transportAddresses[i] = new URIImpl(transportAddresses[i]);
        }
    }

    public AgentId(final URI uri,
                   final URI[] transportAddresses) {
        this.uri = uri;
        this.name = nameFromUri(uri);
        this.transportAddresses = transportAddresses;
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public URI[] getTransportAddresses() {
        return transportAddresses;
    }

    private static URI uriFromName(String name) {
        // attempt to make the name into a URI if it isn't one already.
        // It is not always possible to pass a valid URI as an agent identifier.
        String uriStr = name;
        if (!uriStr.startsWith("http://") && !uriStr.startsWith("urn:")) {
            uriStr = "urn:" + uriStr;
        }

        return new URIImpl(uriStr);
    }

    private static String nameFromUri(final URI uri) {
        String s = uri.stringValue();
        return s.startsWith("http://") ? s.substring(7) : s.startsWith("urn:") ? s.substring(4) : s;
    }
}
