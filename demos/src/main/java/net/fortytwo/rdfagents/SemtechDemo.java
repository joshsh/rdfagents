package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.jade.QueryConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryConsumer;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumer;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 7:12 PM
 */
public class SemtechDemo {

    private void run(final Properties config) throws Exception {

        final RDFAgentsPlatform platform = new RDFAgentsPlatformImpl("fortytwo.net", 8889, config);

        AgentId consumer = new AgentId(
                "urn:agent:consumer@fortytwo.net",
                "xmpp://patabot.1@jabber.org");
        AgentId twitlogic = new AgentId(
                "urn:agent:twitlogic@twitlogic.fortytwo.net",
                "xmpp://patabot.2@jabber.org");

        RDFAgent agent = new RDFAgentImpl(platform, consumer);

        QueryConsumer<Value, Dataset> client = new QueryConsumerImpl(agent);
        PubsubConsumer<Value, Dataset> pubsubConsumer = new PubsubConsumerImpl(agent);

        ConsumerCallback<Dataset> callback = new ConsumerCallback<Dataset>() {
            public void success(final Dataset answer) {
                System.out.println("query has been successfully answered.  Answer follows:");
                try {
                    platform.getDatasetFactory().write(System.out, answer, RDFContentLanguage.RDF_TRIG);
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

        /*
        Object mutex = "";
        synchronized (mutex) {
            mutex.wait(10000);
        }//*/

        //client.submit(new URIImpl("http://xmlns.com/foaf/0.1/Person"), twitlogic, callback);
        pubsubConsumer.submit(new URIImpl("http://rdfs.org/sioc/types#MicroblogPost"), twitlogic, callback);
    }

    private void tmp() throws IOException {
        /*
        AgentId ld = new AgentId("urn:agent:linked-data@fortytwo.net", "xmpp://linked-data@jabber.org");
        AgentId twitter = new AgentId("urn:agent:twitlogic@fortytwo.net", "xmpp://twitlogic@jabber.org");
        AgentId synd = new AgentId("urn:agent:syndicator@fortytwo.net", "xmpp://patabot.1@jabber.org");
        AgentId me = new AgentId("urn:agent:consumer@fortytwo.net", "xmpp://patabot.2@jabber.org");

        RDFAgentsPlatform p = new RDFAgentsPlatformImpl("fortytwo.net", "rdfagents.config");

        RDFAgent agent = new Syndicator(synd, p, ld, twitter);
        */
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

            new SemtechDemo().run(config);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  demo [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:https://github.com/joshsh/rdfagents/wiki>.");
    }
}
