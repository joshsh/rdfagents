package net.fortytwo.rdfagents.model;

import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProviderTmp;
import org.openrdf.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class RDFAgent {
    protected final AgentId identity;

    public RDFAgent(final RDFAgentsPlatform platform,
                    final AgentId id) throws RDFAgentException {

        if (0 == id.getTransportAddresses().length) {
            throw new IllegalArgumentException("at least one transport address must be specified");
        }

        identity = id;
    }

    public AgentId getIdentity() {
        return identity;
    }

    public abstract void setQueryProvider(QueryProvider<Value, Dataset> queryProvider);

    public abstract void setPubsubProvider(PubsubProviderTmp<Value, Dataset> pubsubProvider);

    public static class RDFAgentException extends Exception {
        public RDFAgentException(final Throwable cause) {
            super(cause);
        }
    }

    // TODO: remove/shutdown method
}
