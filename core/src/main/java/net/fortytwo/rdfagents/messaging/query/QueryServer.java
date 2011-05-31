package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.Agent;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.messaging.Role;

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

    protected enum Decision {ANSWER_NOW, ANSWER_LATER, REFUSE}

    protected enum Outcome {SUCCESS, FAILURE}

    private boolean active = true;

    public QueryServer(final Agent agent) {
        super(agent);
    }

    protected abstract Commitment considerQueryRequest(Q query, AgentReference initiator);

    /**
     * Evaluate a query to produce a result.
     * While the query request has previously been accepted or refused based on the identity of the initiator,
     * the actual query is answered independently of the initiator.
     *
     * @param query the query to answer
     * @return a corresponding query result containing either the answer to the query or a failure message
     */
    protected abstract QueryResponse<A> answer(Q query);

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void handle(final Q query,
                       final AgentReference initiator) {
        Commitment c = considerQueryRequest(query, initiator);
    }

    protected Commitment answerNow() {
        return new Commitment(Decision.ANSWER_NOW, null);
    }

    protected Commitment answerLater() {
        return new Commitment(Decision.ANSWER_LATER, null);
    }

    protected Commitment refuse(final String message) {
        return new Commitment(Decision.REFUSE, message);
    }

    /**
     * A commitment (or lack thereof) to answer a query.
     * The query may either be answered immediately (in which case the response to the initiator consists of the query result),
     * or at some later point in time (in which case an "agree" message is first sent to the initiator, to be followed by
     * another message with the query result).
     */
    public class Commitment {
        public final Decision decision;
        public final String message;

        public Commitment(final Decision decision,
                          final String message) {
            this.decision = decision;
            this.message = message;

            if (Decision.REFUSE == decision && (null == message || 0 == message.length())) {
                throw new IllegalArgumentException("a non-empty refusal message must be provided");
            }
        }
    }

}
