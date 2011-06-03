package net.fortytwo.rdfagents.linkeddata;

import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.SailBasedQueryServer;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.ripple.RippleException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * User: josh
 * Date: 5/31/11
 * Time: 3:39 PM
 */
public class LinkedDataAgent extends RDFAgentImpl {
    private final Sail sail;

    public LinkedDataAgent(final Sail baseSail,
                           final String localName,
                           final RDFAgentsPlatform platform,
                           final String... addresses) throws RDFAgentException {
        super(localName, platform, addresses);

        try {
            sail = new LinkedDataSail(baseSail);
            sail.initialize();
        } catch (RippleException e) {
            throw new RDFAgentException(e);
        } catch (SailException e) {
            throw new RDFAgentException(e);
        }

        setQueryServer(new SailBasedQueryServer(this, sail));
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();

        sail.shutDown();
    }
}
