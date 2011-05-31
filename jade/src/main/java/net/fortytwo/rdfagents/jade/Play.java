package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.data.RDFContentLanguage;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.InputStream;

public class Play {

    public static void main(String args[]) {

        try {
            MessageFactory mf = createDemoMessageFactory();

            RDFAgentsPlatform p = new RDFAgentsPlatform("agents.example.org");

            Value subject = uri("http://example.org/ns#arthur");

            AgentReference r1 = new AgentReference(uri("http://example.org/ns#agent1"),
                    new URI[]{uri("mailto:agent1@example.org")});
            AgentReference r2 = new AgentReference(uri("http://example.org/ns#agent2"),
                    new URI[]{uri("mailto:agent2@example.org")});

            RDFAgent.Wrapper w1 = new RDFAgent.Wrapper(r1, mf);
            RDFAgent.Wrapper w2 = new RDFAgent.Wrapper(r1, mf);

            AgentController a1 = p.addAgent("agent1", w1);
            AgentController a2 = p.addAgent("agent2", w2);

            System.out.println("a1.getName(): " + a1.getName());
            a1.putO2AObject(w1.getAgent().submitQuery(subject, r2, null), AgentController.ASYNC);

        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static URI uri(final String s) {
        return new URIImpl(s);
    }

    private static MessageFactory createDemoMessageFactory() throws Exception {
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

        Sail sail = new MemoryStore();
        sail.initialize();
        datasetFactory.addToSail(d, sail);

        MessageFactory messageFactory = new MessageFactory(datasetFactory);
        return messageFactory;
    }
}