package net.fortytwo.rdfagents.data;

import net.fortytwo.rdfagents.model.Dataset;

/**
 * A query which, when evaluated, produces an RDF Dataset.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface DatasetQuery {
    /**
     * @return an RDF Dataset which is an answer to this query
     * @throws DatasetQueryException if query answering fails
     */
    Dataset evaluate() throws DatasetQueryException;

    class DatasetQueryException extends Exception {
        public DatasetQueryException(final Throwable cause) {
            super(cause);
        }
    }
}
