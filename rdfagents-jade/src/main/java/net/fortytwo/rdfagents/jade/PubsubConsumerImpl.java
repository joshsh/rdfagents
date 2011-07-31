package net.fortytwo.rdfagents.jade;

import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumerTmp;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class PubsubConsumerImpl extends PubsubConsumerTmp<Value, Dataset> {

    public PubsubConsumerImpl(final RDFAgent agent) {
        super(agent);

        if (!(agent instanceof RDFAgentImpl)) {
            throw new IllegalArgumentException("expected RDFAgent implementation " + RDFAgentImpl.class.getName()
                    + ", found " + agent.getClass().getName());
        }
    }

    @Override
    public String submit(final Value topic,
                         final AgentId publisher,
                         final ConsumerCallback<Dataset> callback) throws LocalFailure {
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
                       final AgentId publisher,
                       final CancellationCallback callback) throws LocalFailure {
        try {
            RDFJadeAgent.Task t = ((RDFAgentImpl) agent).cancelSubscription(conversationId, publisher, callback);
            ((RDFAgentImpl) agent).putObject(t);
        } catch (StaleProxyException e) {
            throw new LocalFailure(e);
        }
    }
}
