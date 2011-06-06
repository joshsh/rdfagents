package net.fortytwo.rdfagents.messaging.subscribe.old;

import net.fortytwo.rdfagents.model.AgentId;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 5:59 PM
 */
public abstract class SubscribeRequestOld<Q> {
    private final Q topic;
    private final AgentId remoteParticipant;

    /**
     * @param topic       the topic of the desired stream
     * @param remoteParticipant the identity of the remote participant who is requested to provide the stream
     */
    public SubscribeRequestOld(final Q topic,
                               final AgentId remoteParticipant) {
        this.topic = topic;
        this.remoteParticipant = remoteParticipant;
    }

    /**
     * @return the topic of the desired stream
     */
    public Q getTopic() {
        return topic;
    }

    /**
     * @return the identity of the remote participant who is requested to provide the stream
     */
    public AgentId getRemoteParticipant() {
        return remoteParticipant;
    }
}
