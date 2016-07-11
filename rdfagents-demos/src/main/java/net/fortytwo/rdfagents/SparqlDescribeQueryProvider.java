package net.fortytwo.rdfagents;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlDescribeQueryProvider extends QueryProvider<Value, Dataset> {
    private static final String BASE_URI = "http://example.org/baseURI#";

    private final Repository repo;

    public SparqlDescribeQueryProvider(final RDFAgent agent,
                                       final String endpointUrl) throws RDFAgent.RDFAgentException {
        super(agent);

        repo =
                new HTTPRepository(endpointUrl, "");
        try {
            repo.initialize();
        } catch (RepositoryException e) {
            throw new RDFAgent.RDFAgentException(e);
        }
    }

    @Override
    public Commitment considerQueryRequest(final String conversationId,
                                           final Value query,
                                           final AgentId initiator) {
        return new Commitment(Commitment.Decision.AGREE_SILENTLY, null);
    }

    @Override
    public Dataset answer(final Value topic) throws LocalFailure {
        return describeValue(topic);
    }

    @Override
    public void cancel(final String conversationId) throws LocalFailure {
        // Do nothing.
    }

    @Override
    public void finalize() throws Throwable {
        repo.shutDown();
        super.finalize();
    }

    private Dataset describeValue(final Value v) throws LocalFailure {
        if (!(v instanceof URI)) {
            throw new LocalFailure("DESCRIBE for non-URIs is currently not supported");
        }

        String query = "DESCRIBE <" + v + ">";
        Collection<Statement> buffer = new LinkedList<Statement>();
        try {
            try (RepositoryConnection rc = repo.getConnection()) {
                rc.begin();

                GraphQuery q = rc.prepareGraphQuery(QueryLanguage.SPARQL, query);
                GraphQueryResult r = q.evaluate();

                try {
                    while (r.hasNext()) {
                        buffer.add(r.next());
                    }
                } finally {
                    r.close();
                }
            } catch (MalformedQueryException e) {
                throw new LocalFailure(e);
            }
        } catch (RepositoryException | QueryEvaluationException e) {
            throw new LocalFailure(e);
        }

        return new Dataset(buffer);
    }

    private static synchronized CloseableIteration<? extends BindingSet, QueryEvaluationException>
    evaluateQuery(final String queryStr,
                  final SailConnection sc) throws LocalFailure {
        ParsedGraphQuery query;
        try {
            query = QueryParserUtil.parseGraphQuery(QueryLanguage.SPARQL, queryStr, BASE_URI);
        } catch (MalformedQueryException e) {
            throw new LocalFailure(e);
        }

        MapBindingSet bindings = new MapBindingSet();
        boolean includeInferred = false;
        try {
            return sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, includeInferred);
        } catch (SailException e) {
            throw new LocalFailure(e);
        }
    }
}
