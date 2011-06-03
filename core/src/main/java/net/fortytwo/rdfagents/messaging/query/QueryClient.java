package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.RDFAgent;

/**
 * An agent role for making query requests to other agents and handling responses.
 * Queries in RDFAgents follow FIPA's <a href="http://www.fipa.org/specs/fipa00027/index.html">Query Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#query">the specification</a>.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryClient<Q, A> extends Role {
    public QueryClient(final RDFAgent agent) {
        super(agent);
    }

    /**
     * Submits a query to a remote agent, initiating a new Query interaction.
     *
     * @param query             the query to be submitted
     * @param remoteParticipant the remote query server
     * @param callback          a handler for all possible outcomes of the query request.
     *                          Note: its methods typically execute in a thread other than the calling thread.
     * @return the conversation ID, with which the interaction can be tracked and cancelled
     * @throws LocalFailure if the query can't be submitted
     */
    public abstract String submit(Q query,
                                  AgentReference remoteParticipant,
                                  QueryCallback<A> callback) throws LocalFailure;

    /**
     * Submits a cancellation request for a previously submitted query.
     *
     * @param conversationId  the conversation ID of the Query interaction to be cancelled
     * @param remoteParticipant the remote query server
     * @param callback a handler for all possible outcomes of the cancellation request.
     *                 Note: its methods typically execute in a thread other than the calling thread.
     * @throws LocalFailure if cancellation fails locally
     */
    public abstract void cancel(String conversationId,
                                AgentReference remoteParticipant,
                                CancellationCallback callback) throws LocalFailure;

}
