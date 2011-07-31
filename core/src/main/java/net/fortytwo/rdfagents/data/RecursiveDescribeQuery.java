package net.fortytwo.rdfagents.data;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A simple "describes" query engine which produces a RDF Dataset of graphs describing
 * (containing forward or backward links to) the resource, as well as those describing
 * the graphs, and so on recursively.
 * The purpose of the recursion is to capture the provenance trail of the base description of the the resource.
 * <p/>
 * This implementation prevents duplicate statements
 * (i.e. two or more statements with the same subject, predicate, and object in the same graph)
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RecursiveDescribeQuery implements DatasetQuery {
    private final Sail sail;
    private final Value resource;

    /**
     * @param resource the resource to describe
     *                 Exactly what constitutes a description is a matter of implementation.
     * @param sail the storage and inference layer against which to evaluate the query
     */
    public RecursiveDescribeQuery(final Value resource,
                                  final Sail sail) {
        this.resource = resource;
        this.sail = sail;
    }

    public Dataset evaluate() throws DatasetQueryException {
        try {
            SailConnection sc = sail.getConnection();
            try {
                Collection<Statement> c0 = new LinkedList<Statement>();
                Set<Value> alreadyDescribed = new HashSet<Value>();

                addDescriptionTo(c0, resource, sc, alreadyDescribed);

                return deduplicateStatements(new Dataset(c0));
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            throw new DatasetQueryException(e);
        }
    }

    private void addDescriptionTo(final Collection<Statement> addTo,
                                  final Value value,
                                  final SailConnection sc,
                                  final Set<Value> alreadyDescribed) throws SailException {
        alreadyDescribed.add(value);

        Set<Value> toDescribe = new HashSet<Value>();

        // Forward links
        if (value instanceof Resource) {
            addAll(addTo, alreadyDescribed, toDescribe, sc.getStatements((Resource) value, null, null, false));
        }

        // Backlinks
        addAll(addTo, alreadyDescribed, toDescribe, sc.getStatements(null, null, value, false));

        for (Value v : toDescribe) {
            addDescriptionTo(addTo, v, sc, alreadyDescribed);
        }
    }

    private void addAll(final Collection<Statement> addTo,
                        final Set<Value> alreadyDescribed,
                        final Set<Value> toDescribe,
                        final CloseableIteration<? extends Statement, SailException> iter) throws SailException {
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
                addTo.add(st);

                Resource context = st.getContext();
                if (null != context && !alreadyDescribed.contains(context)) {
                    toDescribe.add(context);
                }
            }
        } finally {
            iter.close();
        }
    }

    private Dataset deduplicateStatements(final Dataset original) {
        Set<StatementWrapper> statements = new HashSet<StatementWrapper>();
        Collection<Statement> coll = new LinkedList<Statement>();
        for (Statement s : original.getStatements()) {
            if (statements.add(new StatementWrapper(s))) {
                coll.add(s);
            }
        }
        return new Dataset(coll);
    }

    private class StatementWrapper {
        private final Statement s;

        public StatementWrapper(final Statement s) {
            this.s = s;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof StatementWrapper)) {
                return false;
            }

            Statement s2 = ((StatementWrapper) other).s;
            return s.equals(s2) && (null == s.getContext() ? null == s2.getContext()
                    : null != s2.getContext() && s.getContext().equals(s2.getContext()));
        }

        @Override
        public int hashCode() {
            return s.hashCode() + (null == s.getContext() ? 0 : s.getContext().hashCode());
        }
    }
}
