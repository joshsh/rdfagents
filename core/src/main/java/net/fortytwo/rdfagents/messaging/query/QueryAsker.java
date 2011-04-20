package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.Agent;
import net.fortytwo.rdfagents.messaging.Role;

/**
 * The agent role of posing queries to other, remote agents.
 * Query answering in RDFAgents follows FIPA's <a href="http://www.fipa.org/specs/fipa00027/index.html">Query Interaction Protocol</a>.
 * For more details, see <a href="https://github.com/joshsh/rdfagents/wiki/Queries">the wiki</a>.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryAsker<Q, A> extends Role {
    public QueryAsker(final Agent initiator) {
        super(initiator);
    }

    /**
     * Submits a query to a remote agent, initiating a new conversation which should be completed with a single response from that agent.
     * Query results are handled as soon as a response is received from the remote participant
     *
     * @param request  the query request the be submitted
     * @param callback a handler for the query result.
     *                 Its methods typically execute in a thread other than the calling thread.
     */
    public abstract void submit(QueryRequest<Q> request,
                                QueryCallback<A> callback);

    /**
     * Submits a cancellation request for a previously submitted query.
     *
     * @param request  the query request to be cancelled
     * @param callback a handler for the result of the cancellation request.
     *                 Its methods typically execute in a thread other than the calling thread.
     */
    public abstract void cancel(QueryRequest<Q> request,
                                CancellationCallback callback);

    /**
     * A handler for the result of a query request.
     *
     * @param <A> a class of query answers
     */
    public interface QueryCallback<A> {
        public enum FailureReason {NOT_UNDERSTOOD, REFUSED, REMOTE_FAILURE, CANCELLED, INVALID_RESPONSE}

        /**
         * Indicates success of the query request and provides the query answer.
         *
         * @param answer the answer to the submitted query
         */
        void success(A answer);

        /**
         * Indicates failure of the query request.
         *
         * @param reason      the failure category
         * @param explanation a failure description, in some cases provided by the remote participant
         */
        void failure(FailureReason reason,
                     String explanation);
    }

    /**
     * A handler for the result of a query cancellation request.
     */
    public interface CancellationCallback {
        public enum FailureReason {REMOTE_FAILURE, INVALID_RESPONSE}

        /**
         * Indicates success of the cancellation request.
         */
        void success();

        /**
         * Indicates failure of the cancellation request.
         *
         * @param reason      the failure category
         * @param explanation a failure description, in some cases provided by the remote participant
         */
        void failed(FailureReason reason,
                    String explanation);
    }
}
