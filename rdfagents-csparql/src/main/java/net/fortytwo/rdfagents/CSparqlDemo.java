package net.fortytwo.rdfagents;

import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.engine.CsparqlEngine;
import eu.larkc.csparql.engine.CsparqlEngineImpl;
import eu.larkc.csparql.engine.CsparqlQueryResultProxy;
import net.fortytwo.rdfagents.jade.PubsubConsumerImpl;
import net.fortytwo.rdfagents.jade.RDFAgentImpl;
import net.fortytwo.rdfagents.jade.RDFAgentsPlatformImpl;
import net.fortytwo.rdfagents.jade.testing.EchoCallback;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubConsumerTmp;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CSparqlDemo {

    private void run(final Properties config) throws Exception {

        final RDFAgentsPlatform platform = new RDFAgentsPlatformImpl("fortytwo.net", 8887, config);

        AgentId csparqlagentID = new AgentId(
                "urn:x-agent:csparqlagent@fortytwo.net",
                "xmpp://patabot.1@jabber.org/acc");
        AgentId twitlogic = new AgentId(
                "urn:x-agent:twitlogic@twitlogic.fortytwo.net",
                "xmpp://twitlogic@jabber.org/acc");
        AgentId consumerID = new AgentId(
                "urn:x-agent:consumer@fortytwo.net",
                "xmpp://patabot.1@jabber.org/acc");

        RDFAgent csparqlAgent = new RDFAgentImpl(platform, csparqlagentID);
        RDFAgent consumer = new RDFAgentImpl(platform, consumerID);

//        PubsubConsumer<Value, Dataset> client = new PubsubConsumerImpl(csparqlAgent);
        CSparqlPubsubProvider cSparqlPubsubProvider = new CSparqlPubsubProvider(csparqlAgent, new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"));
        csparqlAgent.setPubsubProvider(cSparqlPubsubProvider);
        
        
        PubsubConsumerTmp<Value, Dataset> endUserPubsubConsumer = new PubsubConsumerImpl(consumer);
        PubsubConsumerTmp<Value, Dataset> cSparqlPubsubConsumer = new PubsubConsumerImpl(csparqlAgent);
        
       
        ConsumerCallback<Dataset> callback = new EchoCallback(platform.getDatasetFactory());
        /*
        Object mutex = "";
        synchronized (mutex) {
            mutex.wait(10000);
        }//*/

        //client.submit(new URIImpl("http://xmlns.com/foaf/0.1/Person"), twitlogic, callback);
        //pubsubConsumer.submit(new URIImpl("http://rdfs.org/sioc/types#MicroblogPost"), twitlogic, callback);

        // TwitLogic query
        //client.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, callback);

        // TwitLogic subscription
//        pubsubConsumer.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, callback);
         
        //TwitLogic with C-SPARQL
        RdfStream st = new CSPARQLConsumer("http://myexample.org/stream");
        
        CsparqlEngine engine = new CsparqlEngineImpl();
		engine.initialize();
		engine.registerStream(st);
        
		
		cSparqlPubsubConsumer.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, (ConsumerCallback<Dataset>) st);
//		cSparqlPubsubConsumer.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), twitlogic, callback);

		endUserPubsubConsumer.submit(new URIImpl("http://twitlogic.fortytwo.net/hashtag/twitter"), csparqlagentID, callback);        
        


		// Register an RDF Stream


		
		CsparqlQueryResultProxy c1 = null;
		
		String query = "REGISTER QUERY HelloWorld AS " +
		"SELECT ?s ?p ?o " +
		"FROM STREAM <http://myexample.org/stream> [RANGE TRIPLES 10] " +
		"WHERE { ?s ?p ?o }";

		try {
			c1 = engine.registerQuery(query);
		} catch (final ParseException ex) {
			System.out.println("errore di parsing: " + ex.getMessage());
		}
		
		// Attach a Result Formatter to the query result proxy 
		
		if (c1 != null) {
			c1.addObserver(cSparqlPubsubProvider);
		}
		
//		engine.unregisterQuery(c1.getId()); 
//	    engine.unregisterStream(st.getIRI()); 
//	    //here nicely stop also RDF
//	    System.exit(0);
        
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

            new CSparqlDemo().run(config);
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
