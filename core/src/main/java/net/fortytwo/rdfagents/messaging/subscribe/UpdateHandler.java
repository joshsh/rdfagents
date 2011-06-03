package net.fortytwo.rdfagents.messaging.subscribe;

import net.fortytwo.rdfagents.messaging.LocalFailure;

/**
* User: josh
* Date: 6/1/11
* Time: 3:30 PM
*/
public interface UpdateHandler<U> {
    void handle(U result) throws LocalFailure;
}
