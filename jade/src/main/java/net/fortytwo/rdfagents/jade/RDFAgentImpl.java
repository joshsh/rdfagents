package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 3:40 PM
 */
public class RDFAgentImpl extends RDFAgent {

    private RDFJadeAgent jadeAgent;
    private AgentController controller;

    public RDFAgentImpl(final String localName,
                        final RDFAgentsPlatformImpl platform,
                        final String... addresses) throws RDFAgentException {
        super(localName, platform, addresses);
    }

    public void setQueryServer(final QueryServer<Value, Dataset> queryServer) {
        jadeAgent.setQueryServer(queryServer);
    }

    public void setJadeAgent(RDFJadeAgent jadeAgent) {
        this.jadeAgent = jadeAgent;
    }

    public void setController(AgentController controller) {
        this.controller = controller;
    }

    public void putObject(final Object obj) throws StaleProxyException {
        controller.putO2AObject(obj, AgentController.ASYNC);
    }

    public RDFJadeAgent.Task submitQuery(final Value resource,
                                         final AgentReference server,
                                         final QueryClient.QueryCallback<Dataset> callback) {
        return jadeAgent.submitQuery(resource, server, callback);
    }

    public RDFJadeAgent.Task cancelQuery(final String conversationId,
                                         final AgentReference server,
                                         final QueryClient.CancellationCallback callback) {
        return jadeAgent.cancelQuery(conversationId, server, callback);
    }
}
