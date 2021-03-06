package net.fortytwo.rdfagents;

import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.SailBasedQueryProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataAgent extends RDFAgentImpl {
    private final Sail sail;

    public LinkedDataAgent(final Sail baseSail,
                           final RDFAgentsPlatform platform,
                           final AgentId id) throws RDFAgentException {
        super(platform, id);

        try {
            sail = new LinkedDataSail(baseSail);
            sail.initialize();
        } catch (SailException e) {
            throw new RDFAgentException(e);
        }

        setQueryProvider(new SailBasedQueryProvider(this, sail));
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();

        sail.shutDown();
    }
}
