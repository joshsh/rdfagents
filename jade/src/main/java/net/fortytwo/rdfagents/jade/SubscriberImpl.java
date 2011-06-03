package net.fortytwo.rdfagents.jade;

import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.subscribe.Subscriber;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 4:49 PM
 */
public class SubscriberImpl extends Subscriber<Value, Dataset> {

    public SubscriberImpl(final RDFAgent agent) {
        super(agent);

        if (!(agent instanceof RDFAgentImpl)) {
            throw new IllegalArgumentException("expected RDFAgent implementation " + RDFAgentImpl.class.getName()
                    + ", found " + agent.getClass().getName());
        }
    }

    @Override
    public String submit(final Value topic,
                         final AgentReference publisher,
                         final QueryCallback<Dataset> callback) throws LocalFailure {
        try {
            RDFJadeAgent.Task t = ((RDFAgentImpl) agent).subscribe(topic, publisher, callback);
            ((RDFAgentImpl) agent).putObject(t);
            return t.getConversationId();
        } catch (StaleProxyException e) {
            throw new LocalFailure(e);
        }
    }

    @Override
    public void cancel(final String conversationId,
                       final AgentReference publisher,
                       final CancellationCallback callback) throws LocalFailure {
        try {
            RDFJadeAgent.Task t = ((RDFAgentImpl) agent).cancelSubscription(conversationId, publisher, callback);
            ((RDFAgentImpl) agent).putObject(t);
        } catch (StaleProxyException e) {
            throw new LocalFailure(e);
        }
    }
}
