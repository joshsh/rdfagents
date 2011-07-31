package net.fortytwo.rdfagents.messaging.subscribe;

import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.RDFAgent;

/**
 * An agent role for subscribing to streams of updates from other agents.
 * Subscriptions in RDFAgents follow FIPA's <a href="http://www.fipa.org/specs/fipa00035/SC00035H.html">Subscribe Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#pubsub">the specification</a>.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class PubsubConsumer<T, U> extends Role {
    public PubsubConsumer(final RDFAgent agent) {
        super(agent);
    }

    /**
     * Submits a subscription request to a remote agent, initiating a new Subscribe interaction.
     *
     * @param topic             the topic of the desired stream
     * @param remoteParticipant the remote publisher
     * @param callback          a handler for all possible outcomes of the subscription request.
     *                          Note: its methods typically execute in a thread other than the calling thread.
     * @return the conversation ID, with which the interaction can be tracked and cancelled
     * @throws LocalFailure if the subscription request can't be submitted
     */
    public abstract String submit(T topic,
                                  AgentId remoteParticipant,
                                  ConsumerCallback<U> callback) throws LocalFailure;

    /**
     * Submits a cancellation request for a previously submitted subscription request.
     * Active and pending subscriptions may be cancelled.
     *
     * @param conversationId    the conversation ID of the Subscribe interaction to be cancelled
     * @param remoteParticipant the remote publisher
     * @param callback          a handler for all possible outcomes of the cancellation request.
     *                          Note: its methods typically execute in a thread other than the calling thread.
     * @throws LocalFailure if cancellation fails locally
     */
    public abstract void cancel(String conversationId,
                                AgentId remoteParticipant,
                                CancellationCallback callback) throws LocalFailure;

}
