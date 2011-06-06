package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.model.AgentId;
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

    public RDFAgentImpl(final RDFAgentsPlatform platform,
                        final AgentId id) throws RDFAgentException {
        super(platform, id);

        if (!(platform instanceof RDFAgentsPlatformImpl)) {
            throw new IllegalArgumentException("expected RDFAgentsPlatform implementation "
                    + RDFAgentsPlatformImpl.class.getName()
                    + ", found " + platform.getClass().getName());
        }

        if (!id.getName().toString().endsWith("@" + platform.getName())) {
            throw new IllegalArgumentException("agent name " + id.getName() + " must end with '@" + platform.getName() + "'");
        }

        String n = id.getName().toString();
        String localName = n.substring(0, n.length() - platform.getName().length());

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

    public void setQueryProvider(final QueryProvider<Value, Dataset> queryProvider) {
        jadeAgent.setQueryProvider(queryProvider);
    }

    @Override
    public void setPubsubProvider(PubsubProvider<Value, Dataset> pubsubProvider) {
        jadeAgent.setPubsubProvider(pubsubProvider);
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
                                         final AgentId server,
                                         final ConsumerCallback<Dataset> callback) {
        return jadeAgent.submitQuery(resource, server, callback);
    }

    public RDFJadeAgent.Task cancelQuery(final String conversationId,
                                         final AgentId server,
                                         final CancellationCallback callback) {
        return jadeAgent.cancelQuery(conversationId, server, callback);
    }

    public RDFJadeAgent.Task subscribe(final Value topic,
                                       final AgentId publisher,
                                       final ConsumerCallback<Dataset> callback) {
        return jadeAgent.subscribe(topic, publisher, callback);
    }

    public RDFJadeAgent.Task cancelSubscription(final String conversationId,
                                                final AgentId publisher,
                                                final CancellationCallback callback) {
        return jadeAgent.cancelSubscription(conversationId, publisher, callback);
    }
}
