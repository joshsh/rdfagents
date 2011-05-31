package net.fortytwo.rdfagents;

import junit.framework.TestCase;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.data.RDFContentLanguage;
import net.fortytwo.rdfagents.jade.MessageFactory;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.InputStream;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 3:41 PM
 */
public abstract class RDFAgentsTestCase extends TestCase {
    protected AgentReference sender, receiver;
    protected URI resourceX = new URIImpl("http://example.org/resourceX");
    protected Literal plainLiteralX = new LiteralImpl("Don't panic.");
    protected Literal typedLiteralX = new LiteralImpl("Don't panic.", XMLSchema.STRING);
    protected Literal languageLiteralX = new LiteralImpl("Don't panic.", "en");

    protected static final String NS = "http://example.org/ns#";
    protected static final URI
            ARTHUR = new URIImpl(NS + "arthur");

    protected Sail sail;
    protected DatasetFactory datasetFactory;
    protected MessageFactory messageFactory;

    @Override
    public void setUp() throws Exception {
        URI senderName = new URIImpl("http://example.org/agentA");
        URI[] senderAddresses = new URI[]{
                new URIImpl("mailto:agentA@example.org"),
                new URIImpl("xmpp:agentA@example.org")};
        sender = new AgentReference(senderName, senderAddresses);

        URI receiverName = new URIImpl("http://example.org/agentB");
        URI[] receiverAddresses = new URI[]{
                new URIImpl("mailto:agentB@example.org"),
                new URIImpl("xmpp:agentB@example.org")};
        receiver = new AgentReference(receiverName, receiverAddresses);

        datasetFactory = new DatasetFactory(new ValueFactoryImpl());
        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            datasetFactory.addLanguage(l);
        }
        InputStream in = RDFAgents.class.getResourceAsStream("dummyData.trig");
        Dataset d;
        try {
            d = datasetFactory.parse(in, RDFContentLanguage.RDF_TRIG);
        } finally {
            in.close();
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
        for (Statement s : d.getStatements()) {
            w.handleStatement(s);
        }
        w.endRDF();
    }
}
