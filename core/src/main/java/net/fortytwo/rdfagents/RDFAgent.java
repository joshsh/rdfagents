package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 1:59 AM
 */
public abstract class RDFAgent {
    protected final AgentReference identity;

    public RDFAgent(final String localName,
                    final RDFAgentsPlatform platform,
                    final String... addresses) throws RDFAgentException {
        URI nameUri;

        try {
            nameUri = uri(localName + "@" + platform.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("local name '" + localName + "' does not complete platform name '"
                    + platform.getName() + "' to form a valid (absolute) URI: " + localName + "@" + platform.getName());
        }

        if (0 == addresses.length) {
            throw new IllegalArgumentException("at least one transport address must be specified");
        }

        URI[] addressUris = new URI[addresses.length];

        int i = 0;
        for (String a : addresses) {
            try {
                addressUris[i++] = uri(a);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("transport address is not a valid (absolute) URI: " + a);
            }
        }

        identity = new AgentReference(nameUri, addressUris);
    }

    public AgentReference getIdentity() {
        return identity;
    }

    public abstract void setQueryServer(QueryServer<Value, Dataset> queryServer);

    public static class RDFAgentException extends Exception {
        public RDFAgentException(final Throwable cause) {
            super(cause);
        }
    }

    // TODO: remove/shutdown method

    private URI uri(final String s) {
        return new URIImpl(s);
    }
}
