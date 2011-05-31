package net.fortytwo.rdfagents.data;

import net.fortytwo.rdfagents.model.Dataset;

/**
 * A query which, when evaluated, produces an RDF Dataset.
 *
 * User: josh
 * Date: 5/27/11
 * Time: 6:12 PM
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
