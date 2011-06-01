package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.ErrorExplanation;
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

    public enum Decision {ANSWER_WITH_CONFIRMATION, ANSWER_WITHOUT_CONFIRMATION, REFUSE}

    public QueryServer(final RDFAgent agent) {
        super(agent);
    }

    public abstract Commitment considerQueryRequest(Q query, AgentReference initiator);

    /**
     * Evaluate a query to produce a result.
     * While the query request has previously been accepted or refused based on the identity of the initiator,
     * the actual query is answered independently of the initiator.
     *
     * @param query the query to answer
     * @return the answer to the query
     * @throws net.fortytwo.rdfagents.messaging.MessageRejectedException if query answering fails
     */
    public abstract A answer(Q query) throws LocalFailure;

    /**
     * A commitment (or lack thereof) to answer a query.
     * The query may either be answered immediately (in which case the response to the initiator consists of the query result),
     * or at some later point in time (in which case an "agree" message is first sent to the initiator, to be followed by
     * another message with the query result).
     */
    public class Commitment {
        public final Decision decision;
        public final ErrorExplanation explanation;

        public Commitment(final Decision decision,
                          final ErrorExplanation explanation) {
            this.decision = decision;
            this.explanation = explanation;
        }
    }

}
