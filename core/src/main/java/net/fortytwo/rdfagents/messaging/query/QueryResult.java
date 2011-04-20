package net.fortytwo.rdfagents.messaging.query;

/**
 * The result of evaluating a query, with two possible outcomes: success (accompanied by a query answer)
 * or failure (accompanied by a failure message).
 *
 * @param <A> a class of query answers
 * @author Joshua Shinavier (http://fortytwo.net).
 */
class QueryResult<A> {
    public final QueryAnswerer.Outcome outcome;
    public final A answer;
    public final String failureMessage;

    public QueryResult(final A answer) {
        this.answer = answer;
        this.outcome = QueryAnswerer.Outcome.SUCCESS;
        this.failureMessage = null;
    }

    public QueryResult(final String failureMessage) {
        this.outcome = QueryAnswerer.Outcome.FAILURE;
        this.answer = null;
        this.failureMessage = failureMessage;
    }
}
