package net.fortytwo.rdfagents.jade.syndication;

import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.jade.QueryConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryConsumer;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumer;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.model.Value;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Syndicator extends RDFAgentImpl {

    private final QueryConsumer<Value, Dataset> queryConsumer;
    private final PubsubConsumer<Value, Dataset> pubsubConsumer;

    public Syndicator(final AgentId id,
                      final RDFAgentsPlatform platform,
                      final AgentId... sources) throws RDFAgentException {
        super(platform, id);

        queryConsumer = new QueryConsumerImpl(this);
        pubsubConsumer = new PubsubConsumerImpl(this);

        setQueryProvider(new SyndicatorQueryProvider(this));
        setPubsubProvider(new SyndicatorPubsubProvider(this));
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
    }

    private class SyndicatorQueryProvider extends QueryProvider<Value, Dataset> {

        public SyndicatorQueryProvider(final RDFAgent agent) {
            super(agent);
        }

        @Override
        public Commitment considerQueryRequest(String conversationId, Value query, AgentId initiator) {
            return new Commitment(Commitment.Decision.AGREE_AND_NOTIFY, null);
        }

        @Override
        public Dataset answer(Value query) throws LocalFailure {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void cancel(String conversationId) throws LocalFailure {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class SyndicatorPubsubProvider extends PubsubProvider<Value, Dataset> {

        public SyndicatorPubsubProvider(final RDFAgent agent) {
            super(agent);
        }

        @Override
        protected Commitment considerSubscriptionRequestInternal(Value topic, AgentId initiator) {
            return new Commitment(Commitment.Decision.AGREE_SILENTLY, null);
        }
    }
}
