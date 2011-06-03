package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.RDFAgent;

/**
 * The agent role of answering queries posed by other agents.
 * Query answering in RDFAgents follows FIPA's <a href="http://www.fipa.org/specs/fipa00027/index.html">Query Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#query">the specification</a>.
 *
 * @param <Q> a class of queries
 * @param <A> a class of query answers
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryServer<Q, A> extends Role {

    public QueryServer(final RDFAgent agent) {
        super(agent);
    }

    /**
     * @param conversationId the conversation of the query
     * @param query          the query to be answered
     * @param initiator      the requester of the query
     * @return the commitment of this server towards answering the query
     */
    public abstract Commitment considerQueryRequest(String conversationId,
                                                    Q query,
                                                    AgentReference initiator);

    /**
     * Evaluate a query to produce a result.
     * While the query request has previously been accepted or refused based on the identity of the initiator,
     * the actual query is answered independently of the initiator.
     *
     * @param query the query to answer
     * @return the answer to the query
     * @throws LocalFailure if query answering fails
     */
    public abstract A answer(Q query) throws LocalFailure;

    /**
     * Cancel a previously submitted query.
     *
     * @param conversationId the conversation of the query
     * @throws LocalFailure if cancellation fails
     */
    public abstract void cancel(String conversationId) throws LocalFailure;
}
