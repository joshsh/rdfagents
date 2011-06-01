package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.messaging.FailureException;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * An agent role for making query requests to other agents and handling responses.
 * Queries in RDFAgents follow FIPA's <a href="http://www.fipa.org/specs/fipa00027/index.html">Query Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#query">the specification</a>.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryClient<Q, A> extends Role {
    public QueryClient(final AgentReference agent) {
        super(agent);
    }

    /**
     * Submits a query to a remote agent, initiating a new query interaction.
     *
     * @param query             the query to be submitted
     * @param remoteParticipant the remote query server
     * @param callback          a handler for all possible outcomes of the query request.
     *                          Note: its methods typically execute in a thread other than the calling thread.
     * @return the conversation ID, with which the interaction can later be cancelled
     * @throws FailureException if the query can't be submitted
     */
    public abstract String submit(Q query,
                                  AgentReference remoteParticipant,
                                  QueryCallback<A> callback) throws FailureException;

    /**
     * Submits a cancellation request for a previously submitted query.
     *
     * @param conversationId  the conversation ID of the query interaction to be cancelled
     * @param remoteParticipant the remote query server
     * @param callback a handler for all possible outcomes of the cancellation request.
     *                 Note: its methods typically execute in a thread other than the calling thread.
     */
    public abstract void cancel(String conversationId,
                                AgentReference remoteParticipant,
                                CancellationCallback callback) throws FailureException;

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
