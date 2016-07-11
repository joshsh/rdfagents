package net.fortytwo.rdfagents;

import junit.framework.TestCase;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.jade.MessageFactory;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.InputStream;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class RDFAgentsTestCase extends TestCase {
    protected AgentId sender, receiver;
    protected IRI resourceX = RDFAgents.createIRI("http://example.org/resourceX");
    protected Literal plainLiteralX = new LiteralImpl("Don't panic.");
    protected Literal typedLiteralX = new LiteralImpl("Don't panic.", XMLSchema.STRING);
    protected Literal languageLiteralX = new LiteralImpl("Don't panic.", "en");

    protected static final String NS = "http://example.org/ns#";
    protected static final IRI
            ARTHUR = RDFAgents.createIRI(NS + "arthur");

    protected Sail sail;
    protected DatasetFactory datasetFactory;
    protected MessageFactory messageFactory;

    @Override
    public void setUp() throws Exception {
        IRI senderName = RDFAgents.createIRI("http://example.org/agentA");
        IRI[] senderAddresses = new IRI[]{
                RDFAgents.createIRI("mailto:agentA@example.org"),
                RDFAgents.createIRI("xmpp:agentA@example.org")};
        sender = new AgentId(senderName, senderAddresses);

        IRI receiverName = RDFAgents.createIRI("http://example.org/agentB");
        IRI[] receiverAddresses = new IRI[]{
                RDFAgents.createIRI("mailto:agentB@example.org"),
                RDFAgents.createIRI("xmpp:agentB@example.org")};
        receiver = new AgentId(receiverName, receiverAddresses);

        datasetFactory = new DatasetFactory(new ValueFactoryImpl());
        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            datasetFactory.addLanguage(l);
        }
        Dataset d;
        try (InputStream in = RDFAgents.class.getResourceAsStream("dummyData.trig")) {
            d = datasetFactory.parse(in, RDFContentLanguage.RDF_TRIG);
        }

        sail = new MemoryStore();
        sail.initialize();
        datasetFactory.addToSail(d, sail);

        messageFactory = new MessageFactory(datasetFactory);
    }

    @Override
    public void tearDown() throws Exception {
        sail.shutDown();
    }

    protected void showDataset(final Dataset d) throws Exception {
        RDFWriter w = Rio.createWriter(RDFFormat.TRIG, System.out);
        w.startRDF();
        d.getStatements().forEach(w::handleStatement);
        w.endRDF();
    }
}
