package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.jade.QueryClientImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.jade.SailBasedQueryServer;
import net.fortytwo.rdfagents.linkeddata.LinkedDataAgent;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.QueryCallback;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.URI;
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
    private final DatasetFactory datasetFactory;

    private void run(final Properties config) throws Exception {

        RDFAgentsPlatform p = new RDFAgentsPlatformImpl("rdfagents.fortytwo.net", datasetFactory, 8888, config);

        RDFAgent a1 = new RDFAgentImpl(RDFAgents.NAME_PREFIX + "agent1", p, "xmpp:patabot.1@fortytwo.net");
        RDFAgent a2 = new RDFAgentImpl(RDFAgents.NAME_PREFIX + "agent2", p, "xmpp:patabot.1@fortytwo.net");
        a2.setQueryServer(new SailBasedQueryServer(a2, sail));

        Sail mem = new MemoryStore();
        mem.initialize();
        RDFAgent aLinked = new LinkedDataAgent(mem, RDFAgents.NAME_PREFIX + "linked-data", p, "xmpp:patabot.1@fortytwo.net");

        QueryClient<Value, Dataset> client = new QueryClientImpl(a1);

        QueryCallback<Dataset> callback = new QueryCallback<Dataset>() {
            public void success(final Dataset answer) {
                System.out.println("query has been successfully answered.  Answer follows:");
                try {
                    datasetFactory.write(System.out, answer, RDFContentLanguage.RDF_TRIG);
                } catch (LocalFailure e) {
                    e.printStackTrace(System.err);
                }
            }

            public void agreed() {
                System.out.println("agreed!");
            }

            public void refused(final ErrorExplanation explanation) {
                System.out.println("refused!");
            }

            public void remoteFailure(final ErrorExplanation explanation) {
                System.out.println("remote failure: " + explanation);
            }

            public void localFailure(final LocalFailure e) {
                System.out.println("local failure: " + e + "\n" + RDFAgents.stackTraceToString(e));
            }
        };

      //  client.submit(new URIImpl("http://example.org/ns#arthur"), a2.getIdentity(), callback);
//        client.submit(new URIImpl("http://identi.ca/user/114"), aLinked.getIdentity(), callback);
        client.submit(new URIImpl("http://xmlns.com/foaf/0.1/Person"), aLinked.getIdentity(), callback);
    }

    private URI uri(final String s) {
        return new URIImpl(s);
    }

    private Demo() throws Exception {
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
