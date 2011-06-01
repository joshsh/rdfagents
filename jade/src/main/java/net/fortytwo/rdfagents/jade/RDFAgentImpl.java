package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import net.fortytwo.rdfagents.RDFAgent;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 3:40 PM
 */
public class RDFAgentImpl extends RDFAgent {

    private RDFAgentJade agentJade;
    private AgentController controller;

    public RDFAgentImpl(final String localName,
                        final RDFAgentsPlatformImpl platform,
                        final String... addresses) throws RDFAgentException {
        super(localName, platform, addresses);
    }

    public void setQueryServer(final QueryServer<Value, Dataset> queryServer) {
        agentJade.setQueryServer(queryServer);
    }

    public RDFAgentJade getAgentJade() {
        return agentJade;
    }

    public void setAgentJade(RDFAgentJade agentJade) {
        this.agentJade = agentJade;
    }

    public AgentController getController() {
        return controller;
    }

    public void setController(AgentController controller) {
        this.controller = controller;
    }
}
