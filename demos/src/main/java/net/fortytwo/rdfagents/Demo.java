package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.jade.QueryConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.jade.SailBasedQueryProvider;
import net.fortytwo.rdfagents.jade.testing.EchoCallback;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.query.QueryConsumer;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 7:12 PM
 */
public class Demo {

    private final Sail sail;

    private void run(final Properties config) throws Exception {

        RDFAgentsPlatform p = new RDFAgentsPlatformImpl("fortytwo.net", 8888, config);

        RDFAgent a1 = new RDFAgentImpl(p,
                new AgentId("urn:x-agent:consumer@fortytwo.net", "xmpp://patabot.1@jabber.org"));
        RDFAgent a2 = new RDFAgentImpl(p,
                new AgentId("urn:x-agent:provider@fortytwo.net", "xmpp://patabot.1@jabber.org"));

        a2.setQueryProvider(new SailBasedQueryProvider(a2, sail));

        Sail baseSail = new MemoryStore();
        baseSail.initialize();

        RDFAgent ld = new LinkedDataAgent(baseSail, p,
                new AgentId("urn:x-agent:linked-data@fortytwo.net", "xmpp://patabot.1@jabber.org"));

        QueryConsumer<Value, Dataset> qc = new QueryConsumerImpl(a1);

        ConsumerCallback<Dataset> callback = new EchoCallback(p.getDatasetFactory());

        qc.submit(new URIImpl("http://example.org/ns#arthur"), a2.getIdentity(), callback);
//        qc.submit(new URIImpl("http://identi.ca/user/114"), aLinked.getIdentity(), callback);
        //qc.submit(new URIImpl("http://xmlns.com/foaf/0.1/Person"), linkedDataId, callback);
    }

    private Demo() throws Exception {
        DatasetFactory datasetFactory = new DatasetFactory(new ValueFactoryImpl());
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
    }

    public static void main(final String args[]) {
        try {
            File props;
            if (1 == args.length) {
                props = new File(args[0]);
            } else {
                props = new File("rdfagents.props");
                //printUsage();
                //System.exit(1);
            }

            Properties config = new Properties();
            InputStream in = new FileInputStream(props);
            try {
                config.load(in);
            } finally {
                in.close();
            }

            new Demo().run(config);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  rdfagents [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:https://github.com/joshsh/rdfagents/wiki>.");
    }
}
