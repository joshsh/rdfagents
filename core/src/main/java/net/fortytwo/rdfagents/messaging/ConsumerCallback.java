package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * A handler for all possible outcomes of a query request.
 *
 * @param <A> a class of query answers
 */
public interface ConsumerCallback<A> {
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
    void localFailure(LocalFailure e);
}
