package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.AgentReference;

/**
 * User: josh
 * Date: 2/25/11
 * Time: 5:59 PM
 */
public abstract class QueryRequest<Q> {
    private final Q query;
    private final AgentReference participant;

    /**
     * @param query       the query to answer
     * @param participant the identity of the remote participant who is requested to answer the query
     */
    public QueryRequest(final Q query,
                        final AgentReference participant) {
        this.query = query;
        this.participant = participant;
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
    public AgentReference getParticipant() {
        return participant;
    }
}
