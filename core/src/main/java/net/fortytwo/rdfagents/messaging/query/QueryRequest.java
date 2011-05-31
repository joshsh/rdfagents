package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.AgentReference;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 5:59 PM
 */
public abstract class QueryRequest<Q> {
    private final Q query;
    private final AgentReference remoteParticipant;

    /**
     * @param query       the query to answer
     * @param remoteParticipant the identity of the remote participant who is requested to answer the query
     */
    public QueryRequest(final Q query,
                        final AgentReference remoteParticipant) {
        this.query = query;
        this.remoteParticipant = remoteParticipant;
    }

    /**
     * @return the query to answer
     */
    public Q getQuery() {
        return query;
    }

    /**
     * @return the identity of the remote participant who is requested to answer the query
     */
    public AgentReference getRemoteParticipant() {
        return remoteParticipant;
    }
}
