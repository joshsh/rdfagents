package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
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
    private final RDFAgent agent;
    private final AgentController controller;

    public QueryClientImpl(final AgentReference client,
                           final RDFAgent agent,
                           final AgentController controller) {
        super(client);
        this.agent = agent;
        this.controller = controller;
    }

    @Override
    public String submit(Value query,
                         AgentReference remoteParticipant,
                         QueryCallback<Dataset> callback) throws FailureException {
        try {
            RDFAgent.Task t = agent.submitQuery(query, remoteParticipant, callback);
            controller.putO2AObject(t, AgentController.ASYNC);
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
            RDFAgent.Task t = agent.cancelQuery(conversationId, remoteParticipant, callback);
            controller.putO2AObject(t, AgentController.ASYNC);
        } catch (StaleProxyException e) {
            throw new FailureException(e);
        }
    }
}
