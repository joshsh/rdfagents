package net.fortytwo.rdfagents.jade;

import net.fortytwo.rdfagents.data.DatasetQuery;
import net.fortytwo.rdfagents.data.RecursiveDescribeQuery;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Value;
import org.openrdf.sail.Sail;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 10:55 AM
 */
public class SailBasedQueryServer extends QueryServer<Value, Dataset> {
    private final Sail sail;

    public SailBasedQueryServer(final RDFAgent agent,
                                final Sail sail) {
        super(agent);

        this.sail = sail;
    }

    @Override
    public Commitment considerQueryRequest(final Value query,
                                              final AgentReference initiator) {
        return new Commitment(Decision.ANSWER_WITHOUT_CONFIRMATION, null);
    }

    @Override
    public Dataset answer(final Value query) throws LocalFailure {
        try {
            return new RecursiveDescribeQuery(query, sail).evaluate();
        } catch (DatasetQuery.DatasetQueryException e) {
            throw new LocalFailure(e);
        }
    }
}
