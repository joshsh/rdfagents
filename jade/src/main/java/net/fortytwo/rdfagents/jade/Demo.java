package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.data.RDFContentLanguage;
import net.fortytwo.rdfagents.messaging.FailureException;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.messaging.query.QueryServer;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
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
    private final MessageFactory messageFactory;

    private void run(final Properties config) throws Exception {

        String agent1Name = config.getProperty("agent1.name");
        String agent1Address = config.getProperty("agent1.xmpp.address");
        String agent2Name = config.getProperty("agent2.name");
        String agent2Address = config.getProperty("agent2.xmpp.address");

        RDFAgentsPlatform p = new RDFAgentsPlatform("agents.example.org", 8888, config);
        // System.exit(1);
        Value subject = uri("http://example.org/ns#arthur");

        AgentReference r1 = new AgentReference(uri(agent1Name),
                new URI[]{uri(agent1Address)});
        AgentReference r2 = new AgentReference(uri(agent2Name),
                new URI[]{uri(agent2Address)});

        QueryServer<Value, Dataset> s1 = new SailBasedQueryServer(r1, sail);
        QueryServer<Value, Dataset> s2 = new SailBasedQueryServer(r2, sail);

        RDFAgent.Wrapper w1 = new RDFAgent.Wrapper(r1, messageFactory, s1);
        RDFAgent.Wrapper w2 = new RDFAgent.Wrapper(r2, messageFactory, s2);

        AgentController a1 = p.addAgent("urn:agent1", w1);
        AgentController a2 = p.addAgent("urn:agent2", w2);

        System.out.println("a1.getName(): " + a1.getName());

        QueryClient<Value, Dataset> client = new QueryClientImpl(r1, w1.getAgent(), a1);
        QueryClient.QueryCallback<Dataset> callback = new QueryClient.QueryCallback<Dataset>() {
            public void success(final Dataset answer) {
                System.out.println("query has been successfully answered.  Answer follows:");
                try {
                    datasetFactory.write(System.out, answer, RDFContentLanguage.RDF_TRIG);
                } catch (FailureException e) {
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

            public void localFailure(final Exception e) {
                System.out.println("local failure: " + e);
            }
        };
        client.submit(subject, r2, callback);


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

        messageFactory = new MessageFactory(datasetFactory);
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
