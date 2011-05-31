package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.Agent;
import net.fortytwo.rdfagents.messaging.Role;

/**
 * An agent role for making query requests to other agents and handling responses.
 * Queries in RDFAgents follow FIPA's <a href="http://www.fipa.org/specs/fipa00027/index.html">Query Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#query">the specification</a>.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryClient<Q, A> extends Role {
    public QueryClient(final Agent client) {
        super(client);
    }

    /**
     * Submits a query to a remote agent, initiating a new query interaction.
     *
     * @param request  the query request the be submitted
     * @param callback a handler for all possible outcomes of the query request.
     *                 Note: its methods typically execute in a thread other than the calling thread.
     */
    public abstract void submit(QueryRequest<Q> request,
                                QueryCallback<A> callback);

    /**
     * Submits a cancellation request for a previously submitted query.
     *
     * @param request  the previously submitted query request to be cancelled
     * @param callback a handler for all possible outcomes of the cancellation request.
     *                 Note: its methods typically execute in a thread other than the calling thread.
     */
    public abstract void cancel(QueryRequest<Q> request,
                                CancellationCallback callback);

    /**
     * A handler for all possible outcomes of a query request.
     *
     * @param <A> a class of query answers
     */
    public interface QueryCallback<A> {
        /**
         * Indicates success of the query request and provides the query answer.
         *
         * @param answer the answer to the submitted query
         */
        void success(A answer);

        /**
         * Indicates that the remote participant has agreed to answer the query.
         */
        void agreed();

        /**
         * Indicates that the remote participant has refused to answer the query.
         *
         * @param explanation an explanation of the refusal, provided by the remote participant
         */
        void refused(ErrorExplanation explanation);

        /**
         * Indicates that the remote participant has failed to answer the query.
         *
         * @param explanation an explanation of failure, provided by the remote participant
         */
        void remoteFailure(ErrorExplanation explanation);

        /**
         * Indicates that a local exception has caused this interaction to fail.
         *
         * @param e the local exception which has occurred
         */
        void localFailure(Exception e);
    }

    /**
     * A handler for all possible outcomes of a query cancellation request.
     */
    public interface CancellationCallback {
        /**
         * Indicates success of the cancellation request.
         */
        void success();

        /**
         * Indicates failure of the cancellation request.
         *
         * @param explanation an explanation of failure, provided by the remote participant
         */
        void remoteFailure(ErrorExplanation explanation);

        /**
         * Indicates that a local exception has caused this interaction to fail.
         *
         * @param e the local exception which has occurred
         */
        void localFailure(Exception e);
    }
}
