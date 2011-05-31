package net.fortytwo.rdfagents.messaging.query;

/**
 * The result of evaluating a query, with two possible outcomes: success (accompanied by a query answer)
 * or failure (accompanied by a failure message).
 *
 * @param <A> a class of query answers
 * @author Joshua Shinavier (http://fortytwo.net).
 */
class QueryResponse<A> {
    public final QueryServer.Outcome outcome;
    public final A answer;
    public final String failureMessage;

    public QueryResponse(final A answer) {
        this.answer = answer;
        this.outcome = QueryServer.Outcome.SUCCESS;
        this.failureMessage = null;
    }

    public QueryResponse(final String failureMessage) {
        this.outcome = QueryServer.Outcome.FAILURE;
        this.answer = null;
        this.failureMessage = failureMessage;
    }
}
