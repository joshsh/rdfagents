package net.fortytwo.rdfagents;

import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.jade.QueryConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.jade.testing.EchoCallback;
import net.fortytwo.rdfagents.messaging.CancellationCallback;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.query.QueryConsumer;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumer;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 7:12 PM
 */
public class SemtechDemo {

    private void run(final Properties config) throws Exception {

        final RDFAgentsPlatform platform = new RDFAgentsPlatformImpl("fortytwo.net", 8887, config);

        AgentId consumer = new AgentId(
                "urn:x-agent:consumer@fortytwo.net",
                "xmpp://patabot.1@jabber.org/acc");
        AgentId twitlogic = new AgentId(
                "urn:x-agent:twitlogic@twitlogic.fortytwo.net",
                "xmpp://twitlogic@jabber.org/acc");

        RDFAgent agent = new RDFAgentImpl(platform, consumer);

        QueryConsumer<Value, Dataset> client = new QueryConsumerImpl(agent);
        PubsubConsumer<Value, Dataset> pubsubConsumer = new PubsubConsumerImpl(agent);

        ConsumerCallback<Dataset> callback = new EchoCallback(platform.getDatasetFactory());
        /*
        Object mutex = "";
        synchronized (mutex) {
            mutex.wait(10000);
        }//*/

        //client.submit(new URIImpl("http://xmlns.com/foaf/0.1/Person"), twitlogic, callback);
        //pubsubConsumer.submit(new URIImpl("http://rdfs.org/sioc/types#MicroblogPost"), twitlogic, callback);

        // TwitLogic query
        client.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, callback);

        // TwitLogic subscription
        String conv = null;
        try {
            conv = pubsubConsumer.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, callback);
        } finally {
            if (conv != null) {
                pubsubConsumer.cancel(conv, twitlogic, new CancellationCallback() {
                    @Override
                    public void success() {
                        System.out.println("cancelled!");
                    }

                    @Override
                    public void remoteFailure(ErrorExplanation explanation) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void localFailure(LocalFailure e) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
            }
        }
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
