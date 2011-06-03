package net.fortytwo.rdfagents.messaging;

import net.fortytwo.rdfagents.model.ErrorExplanation;

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
    void localFailure(LocalFailure e);
}
