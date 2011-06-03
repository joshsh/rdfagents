package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.messaging.subscribe.Publisher;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
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
                        final RDFAgentsPlatform platform,
                        final String... addresses) throws RDFAgentException {
        super(localName, platform, addresses);

        if (!(platform instanceof RDFAgentsPlatformImpl)) {
            throw new IllegalArgumentException("expected RDFAgentsPlatform implementation "
                    + RDFAgentsPlatformImpl.class.getName()
                    + ", found " + platform.getClass().getName());
        }

        MessageFactory messageFactory = new MessageFactory(platform.getDatasetFactory());
        RDFJadeAgent.Wrapper w = new RDFJadeAgent.Wrapper(getIdentity(), messageFactory);

        try {
            RDFAgentsPlatformImpl.CondVar startUpLatch = new RDFAgentsPlatformImpl.CondVar();

            AgentController c = ((RDFAgentsPlatformImpl) platform).getContainer().createNewAgent(localName, RDFJadeAgent.class.getName(),
                    new Object[]{startUpLatch, w});
            c.start();

            // Wait until the agent starts up and notifies the Object
            startUpLatch.waitOn();

            setController(c);
        } catch (StaleProxyException e) {
            throw new RDFAgent.RDFAgentException(e);
        } catch (InterruptedException e) {
            throw new RDFAgent.RDFAgentException(e);
        }

        setJadeAgent(w.getJadeAgent());
    }

    public void setQueryServer(final QueryServer<Value, Dataset> queryServer) {
        jadeAgent.setQueryServer(queryServer);
    }

    @Override
    public void setPublisher(Publisher<Value, Dataset> publisher) {
        throw new UnsupportedOperationException("not yet implemented");
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
                                         final QueryCallback<Dataset> callback) {
        return jadeAgent.submitQuery(resource, server, callback);
    }

    public RDFJadeAgent.Task cancelQuery(final String conversationId,
                                         final AgentReference server,
                                         final CancellationCallback callback) {
        return jadeAgent.cancelQuery(conversationId, server, callback);
    }

    public RDFJadeAgent.Task subscribe(final Value topic,
                                       final AgentReference publisher,
                                       final QueryCallback<Dataset> callback) {
        return jadeAgent.subscribe(topic, publisher, callback);
    }

    public RDFJadeAgent.Task cancelSubscription(final String conversationId,
                                                final AgentReference publisher,
                                                final CancellationCallback callback) {
        return jadeAgent.cancelSubscription(conversationId, publisher, callback);
    }
}
