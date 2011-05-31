package net.fortytwo.rdfagents.sail;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

/**
 * User: josh
 * Date: 5/24/11
 * Time: 7:02 PM
 */
public class AgentSail implements StackableSail {
    private final Sail baseSail;

    public AgentSail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

    public void setBaseSail(final Sail sail) {
        throw new UnsupportedOperationException();
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setDataDir(final File file) {
        throw new UnsupportedOperationException();
    }

    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    public void initialize() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutDown() throws SailException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWritable() throws SailException {
        return false;
    }

    public SailConnection getConnection() throws SailException {
        return new AgentSailConnection();
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }
}
