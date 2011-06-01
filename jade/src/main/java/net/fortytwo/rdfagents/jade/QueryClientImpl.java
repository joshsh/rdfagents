package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.RDFAgent;
import net.fortytwo.rdfagents.messaging.FailureException;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 9:36 AM
 */
public class QueryClientImpl extends QueryClient<Value, Dataset> {
    private final RDFAgentImpl wrapper;

    public QueryClientImpl(final RDFAgent agent) {
        super(agent.getIdentity());

        if (agent instanceof RDFAgentImpl) {
            this.wrapper = (RDFAgentImpl) agent;
        } else {
            throw new IllegalArgumentException("expected RDFAgent implementation " + RDFAgentImpl.class.getName()
                    + ", found " + agent.getClass().getName());
        }
    }

    @Override
    public String submit(Value query,
                         AgentReference remoteParticipant,
                         QueryCallback<Dataset> callback) throws FailureException {
        try {
            RDFAgentJade.Task t = wrapper.getAgentJade().submitQuery(query, remoteParticipant, callback);
            wrapper.getController().putO2AObject(t, AgentController.ASYNC);
            return t.getConversationId();
        } catch (StaleProxyException e) {
            throw new FailureException(e);
        }
    }

    @Override
    public void cancel(final String conversationId,
                       final AgentReference remoteParticipant,
                       final CancellationCallback callback) throws FailureException {
        try {
            RDFAgentJade.Task t = wrapper.getAgentJade().cancelQuery(conversationId, remoteParticipant, callback);
            wrapper.getController().putO2AObject(t, AgentController.ASYNC);
        } catch (StaleProxyException e) {
            throw new FailureException(e);
        }
    }
}
