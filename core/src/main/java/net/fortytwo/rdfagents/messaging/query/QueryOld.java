package net.fortytwo.rdfagents.messaging.query;

import net.fortytwo.rdfagents.model.Agent;

/**
 * ...an asynchronous request-response messaging pattern...
 *
 * @param <Q> a class of queries
 * @param <R> a class of query responses
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class QueryOld<Q, R> {
    private final Agent recipient;
    private final Q query;
    private final ResultHandler<R> resultHandler;
    private final FailureHandler failureHandler;

    public QueryOld(final Agent recipient,
                    final Q query,
                    final ResultHandler<R> resultHandler) {
        this.recipient = recipient;
        this.query = query;
        this.resultHandler = resultHandler;
        this.failureHandler = null;
    }

    public QueryOld(final Agent recipient,
                    final Q query,
                    final ResultHandler<R> resultHandler,
                    final FailureHandler failureHandler) {
        this.recipient = recipient;
        this.query = query;
        this.resultHandler = resultHandler;
        this.failureHandler = failureHandler;
    }

    public Agent getRecipient() {
        return recipient;
    }

    public Q getQuery() {
        return query;
    }

    public abstract void submit();

    public interface ResultHandler<R> {
        void received(R result);
    }

    public interface FailureHandler {
        void refused();

        void failed();
    }
}
