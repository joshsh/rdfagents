package net.fortytwo.rdfagents.messaging.subscribe;

import net.fortytwo.rdfagents.messaging.LocalFailure;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface UpdateHandler<U> {
    void handle(U result) throws LocalFailure;
}
