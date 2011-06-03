package net.fortytwo.rdfagents.jade;

import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 9:36 AM
 */
public class QueryClientImpl extends QueryClient<Value, Dataset> {

    public QueryClientImpl(final RDFAgent agent) {
        super(agent);

        if (!(agent instanceof RDFAgentImpl)) {
            throw new IllegalArgumentException("expected RDFAgent implementation " + RDFAgentImpl.class.getName()
                    + ", found " + agent.getClass().getName());
        }
    }

    @Override
    public String submit(Value query,
                         AgentReference remoteParticipant,
                         QueryCallback<Dataset> callback) throws LocalFailure {
        try {
            RDFJadeAgent.Task t = ((RDFAgentImpl) agent).submitQuery(query, remoteParticipant, callback);
            ((RDFAgentImpl) agent).putObject(t);
            return t.getConversationId();
        } catch (StaleProxyException e) {
            throw new LocalFailure(e);
        }
    }

    @Override
    public void cancel(final String conversationId,
                       final AgentReference remoteParticipant,
                       final CancellationCallback callback) throws LocalFailure {
        try {
            RDFJadeAgent.Task t = ((RDFAgentImpl) agent).cancelQuery(conversationId, remoteParticipant, callback);
            ((RDFAgentImpl) agent).putObject(t);
        } catch (StaleProxyException e) {
            throw new LocalFailure(e);
        }
    }
}