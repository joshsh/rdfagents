package net.fortytwo.rdfagents.jade;

import net.fortytwo.rdfagents.data.DatasetQuery;
import net.fortytwo.rdfagents.data.RecursiveDescribeQuery;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;
import org.openrdf.sail.Sail;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 10:55 AM
 */
public class SailBasedQueryProvider extends QueryProvider<Value, Dataset> {
    private final Sail sail;

    public SailBasedQueryProvider(final RDFAgent agent,
                                  final Sail sail) {
        super(agent);

        this.sail = sail;
    }

    @Override
    public Commitment considerQueryRequest(final String conversationId,
                                           final Value query,
                                           final AgentId initiator) {
        return new Commitment(Commitment.Decision.AGREE_SILENTLY, null);
    }

    @Override
    public Dataset answer(final Value query) throws LocalFailure {
        System.out.println("### got a query: " + query);
        try {
            return new RecursiveDescribeQuery(query, sail).evaluate();
        } catch (DatasetQuery.DatasetQueryException e) {
            throw new LocalFailure(e);
        }
    }

    @Override
    public void cancel(final String conversationId) throws LocalFailure {
        // Do nothing.  A reasonable timeout policy is assumed,
        // so that it is not the responsibility of the client to cancel queries which run too long.
    }
}
