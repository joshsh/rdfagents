package net.fortytwo.rdfagents.messaging.subscribe.old;

import net.fortytwo.rdfagents.model.AgentReference;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 5:59 PM
 */
public abstract class SubscribeRequestOld<Q> {
    private final Q topic;
    private final AgentReference remoteParticipant;

    /**
     * @param topic       the topic of the desired stream
     * @param remoteParticipant the identity of the remote participant who is requested to provide the stream
     */
    public SubscribeRequestOld(final Q topic,
                               final AgentReference remoteParticipant) {
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
    public AgentReference getRemoteParticipant() {
        return remoteParticipant;
    }
}
