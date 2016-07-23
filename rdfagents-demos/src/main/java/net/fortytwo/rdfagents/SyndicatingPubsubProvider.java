package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumer;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: not yet tested
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class SyndicatingPubsubProvider extends PubsubProvider<Value, Dataset> {
    private static final Logger logger = Logger.getLogger(SyndicatingPubsubProvider.class.getName());

    private final PubsubConsumer<Value, Dataset> consumer;
    private final AgentId[] others;

    public SyndicatingPubsubProvider(final RDFAgent agent,
                                     final AgentId... others) {
        super(agent);

        this.others = others;
        this.consumer = new PubsubConsumerImpl(this.agent);
    }

    @Override
    protected Commitment considerSubscriptionRequestInternal(final Value topic,
                                                             final AgentId initiator) {
        for (AgentId a : others) {
            ConsumerCallback<Dataset> callback = new ConsumerCallback<Dataset>() {

                @Override
                public void success(final Dataset answer) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void agreed() {
                    // Do nothing.
                }

                @Override
                public void refused(ErrorExplanation explanation) {
                    logger.warning("subscription request refused: " + explanation);
                }

                @Override
                public void remoteFailure(ErrorExplanation explanation) {
                    logger.warning("remote failure: " + explanation);
                }

                @Override
                public void localFailure(LocalFailure e) {
                    logger.log(Level.SEVERE, "local failure: " + e.getMessage(), e);
                }
            };

            // TODO: cancel subscriptions based on downstream cancellations
            if (!getTopics().contains(topic)) {
                try {
                    consumer.submit(topic, a, callback);
                } catch (LocalFailure e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        return new Commitment(Commitment.Decision.AGREE_SILENTLY, null);
    }
}
