package net.fortytwo.rdfagents.jade.sesame.testing;

import jade.content.ContentManager;
import jade.content.abs.AbsIRE;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import net.fortytwo.rdfagents.jade.onto.RDFAgentsOntology;

import java.util.Random;

/**
 * User: josh
 * Date: 4/22/11
 * Time: 11:47 PM
 */
public class ExampleMessages {
    private static final String EXAMPLE_RDF = "@prefix dbpprop: <http://dbpedia.org/property/> .\n" +
            "@prefix dbpedia-owl: <http://dbpedia.org/ontology/> .\n" +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "\n" +
            "<http://dbpedia.org/resource/Beijing>\n" +
            "    dbpprop:name \"Beijing\"@en ;\n" +
            "    dbpedia-owl:country <http://dbpedia.org/resource/China> ;\n" +
            "    owl:sameAs\n" +
            "        <http://data.nytimes.com/49823253852479839961>,\n" +
            "\t<http://dbpedia.org/resource/Beijing>,\n" +
            "\t<http://rdf.freebase.com/ns/m/01914>,\n" +
            "\t<http://rdf.freebase.com/ns/m/047txg>,\n" +
            "\t<http://sws.geonames.org/1816670/> ;\n" +
            "    [...]";

    public static void main(final String[] args) throws Exception {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int n = r.nextInt(16);
            if (n < 10) {
                sb.append(n);
            } else {
                sb.append((char) ('A' + n - 10));
            }
        }
        String convId = sb.toString();

        AID client = new AID();
        client.setName("http://fortytwo.net/agents/smith");
        client.addAddresses("xmpp:smith@fortytwo.net");

        AID server = new AID();
        server.setName("http://example.org/agents/rdfnews");
        server.addAddresses("xmpp:rdfnews@example.org");

        ContentManager manager = new ContentManager();
        Codec codec = new SLCodec(0);
        manager.registerLanguage(codec);
        manager.registerOntology(BasicOntology.getInstance());
        for (String s : manager.getLanguageNames()) {
            System.out.println("language: " + s);
        }
        for (String s : manager.getOntologyNames()) {
            System.out.println("ontology: " + s);
        }
        ACLMessage m;

        m = new ACLMessage(ACLMessage.INFORM);
        m.setSender(client);
        m.addReceiver(server);
        print(m);

        m = new ACLMessage(ACLMessage.QUERY_REF);
        m.setSender(client);
        m.addReceiver(server);
        m.setLanguage(codec.getName());
        m.setOntology(RDFAgentsOntology.getInstance().getName());
        m.setConversationId(convId);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        m.setContent("(describe (resource \"http://dbpedia.org/resource/Beijing\"))");
        print(m);

        m = new ACLMessage(ACLMessage.INFORM_REF);
        m.setSender(server);
        m.addReceiver(client);
        m.setLanguage("rdf-nquads");
        m.setConversationId(convId);
        m.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        //m.setEncoding("UTF-8");
        m.setContent(EXAMPLE_RDF);
        print(m);

        m = new ACLMessage(ACLMessage.SUBSCRIBE);
        m.setSender(client);
        m.addReceiver(server);
        m.setConversationId(convId);
        //AbsContentElementList l = new AbsContentElementList();
        //l.add(new AbsAgentAction("BOGUS"));
        //m.setEncoding("fooasasd");
        //ContentElement c = new Modify();
        AbsIRE c = new AbsIRE("IOTA");
        //c.s
        System.out.println("is a content expression: " + c.isAContentExpression());
        //manager.fillContent(m, c);
        //m.setContentObject(c);
        //m.setContent("content...");
        m.setOntology(RDFAgentsOntology.getInstance().getName());
        print(m);

        m = new ACLMessage(ACLMessage.AGREE);
        m.setSender(server);
        m.addReceiver(client);
        m.setConversationId(convId);
        print(m);
    }

    private static void print(final ACLMessage m) {
        //System.out.println(m.toString());

        StringACLCodec c = new StringACLCodec();
        byte[] bytes = c.encode(m, "UTF-8");
        System.out.println(new String(bytes));
    }
}
