package net.fortytwo.rdfagents.sail;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class AgentSailConnection implements SailConnection {
    public boolean isOpen() throws SailException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, Dataset dataset, BindingSet bindingSet, boolean b) throws SailException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(Resource resource, URI uri, Value value, boolean b, Resource... resources) throws SailException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long size(Resource... resources) throws SailException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void commit() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void rollback() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addStatement(Resource resource, URI uri, Value value, Resource... resources) throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeStatements(Resource resource, URI uri, Value value, Resource... resources) throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clear(Resource... resources) throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespace(String s) throws SailException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setNamespace(String s, String s1) throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeNamespace(String s) throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearNamespaces() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
