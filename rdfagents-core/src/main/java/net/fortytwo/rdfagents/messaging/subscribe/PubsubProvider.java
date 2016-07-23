package net.fortytwo.rdfagents.messaging.subscribe;

import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.Role;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.RDFAgent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The agent role of producing streams of updates based on topic-based subscriptions from other agents.
 * Subscriptions in RDFAgents follow FIPA's
 * <a href="http://www.fipa.org/specs/fipa00035/SC00035H.html">Subscribe Interaction Protocol</a>.
 * For more details, see <a href="http://fortytwo.net/2011/rdfagents/spec#pubsub">the specification</a>.
 *
 * @param <T> a class of topics
 * @param <U> a class of updates
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class PubsubProvider<T, U> extends Role {
    private static final Logger logger = Logger.getLogger(PubsubProvider.class.getName());

    private class Subscription {
        public final AgentId subscriber;
        public final T topic;
        public final UpdateHandler<U> handler;

        public Subscription(final AgentId subscriber,
                            final T topic,
                            final UpdateHandler<U> handler) {
            this.subscriber = subscriber;
            this.topic = topic;
            this.handler = handler;
        }
    }

    private final Map<T, Set<String>> idsByTopic;
    private final Map<String, Subscription> subscriptionsById;
    private final Object mutex = "";

    public PubsubProvider(final RDFAgent agent) {
        super(agent);
        this.idsByTopic = new HashMap<>();
        this.subscriptionsById = new HashMap<>();
    }

    /**
     * Renders a decision with respect to a subscription request.
     *
     * @param conversationId the conversation of the subscription
     * @param topic          the topic of the desired stream
     * @param initiator      the requester of the subscription
     * @param handler        the handler for updates, provided this subscription request is accepted
     * @return the commitment of this server towards handling the subscription
     * @throws LocalFailure if decision making fails
     */
    public Commitment considerSubscriptionRequest(final String conversationId,
                                                  final T topic,
                                                  final AgentId initiator,
                                                  final UpdateHandler<U> handler) throws LocalFailure {
        Commitment c = considerSubscriptionRequestInternal(topic, initiator);

        switch (c.getDecision()) {
            case AGREE_AND_NOTIFY:
                synchronized (mutex) {
                    Set<String> ids = idsByTopic.get(topic);
                    if (null == ids) {
                        ids = new HashSet<>();
                        idsByTopic.put(topic, ids);
                    }
                    ids.add(conversationId);

                    Subscription s = new Subscription(initiator, topic, handler);
                    subscriptionsById.put(conversationId, s);
                }

                return c;
            case AGREE_SILENTLY:
                throw new LocalFailure("agreeing to a subscription without confirmation is not supported");
            case REFUSE:
                return c;
            default:
                throw new LocalFailure("unexpected decision: " + c.getDecision());
        }
    }

    /**
     * Renders a decision with respect to a subscription request.
     *
     * @param topic     the topic of the desired stream
     * @param initiator the requester of the subscription
     * @return the commitment of this server towards handling the subscription
     */
    protected abstract Commitment considerSubscriptionRequestInternal(T topic,
                                                                      AgentId initiator);

    /**
     * Cancel a subscription.
     *
     * @param conversationId the conversation of the subscription
     * @throws LocalFailure if cancellation fails
     */
    public void cancel(String conversationId) throws LocalFailure {
        synchronized (mutex) {
            Subscription s = subscriptionsById.get(conversationId);

            if (null == s) {
                logger.warning("attempted to cancel a Subscribe interaction which does not exist: " + conversationId);
            } else {
                subscriptionsById.remove(conversationId);
                Set<String> ids = idsByTopic.get(s.topic);
                if (null != ids) {
                    if (1 >= ids.size()) {
                        idsByTopic.remove(s.topic);
                    } else {
                        ids.remove(conversationId);
                    }
                }
            }
        }
    }

    /**
     * Communicates an update for each matching subscription to its respective subscriber
     *
     * @param topic  the topic of the subscription(s)
     * @param update the update to communicate
     * @throws LocalFailure if update communication fails
     */
    protected void produceUpdate(final T topic,
                                 final U update) throws LocalFailure {
        for (String id : idsByTopic.get(topic)) {
            Subscription s = subscriptionsById.get(id);
            s.handler.handle(update);
        }
    }

    /**
     * @return the set of all topics of active subscriptions
     */
    protected Set<T> getTopics() {
        return idsByTopic.keySet();
    }
}
