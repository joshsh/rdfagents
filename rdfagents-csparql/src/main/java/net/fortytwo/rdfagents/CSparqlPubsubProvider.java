package net.fortytwo.rdfagents;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.common.streams.format.GenericObservable;
import eu.larkc.csparql.common.streams.format.GenericObserver;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProvider;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import java.util.ArrayList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CSparqlPubsubProvider extends PubsubProvider<Value, Dataset> implements GenericObserver<RDFTable> {

    private Value topic;

    public CSparqlPubsubProvider(RDFAgent agent, Value topic) {
        super(agent);
        this.topic = topic;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Commitment considerSubscriptionRequestInternal(Value arg0,
                                                             AgentId arg1) {
        return new Commitment(Commitment.Decision.AGREE_AND_NOTIFY, null);
    }

    @Override
    public void update(GenericObservable<RDFTable> observed, RDFTable q) {

        System.out.println("************==============***********");


        ArrayList<Statement> statements = new ArrayList<>();

        for (RDFTuple t : q) {
            String subject = t.get(0);
            String property = t.get(1);
            String object = t.get(2);

            if (object.contains("\"")) {
                Literal l = RDFAgents.createLiteral(object.replaceAll("\"", ""));
                statements.add(RDFAgents.createStatement(RDFAgents.createIRI(subject),
                        RDFAgents.createIRI(property), l));
            } else {

                statements.add(RDFAgents.createStatement(RDFAgents.createIRI(subject),
                        RDFAgents.createIRI(property), RDFAgents.createIRI(object)));
            }
        }

        try {
            this.produceUpdate(topic, new Dataset(statements));

            System.out.println("number of statements:" + statements.size());
            System.out.println("************==============***********");
        } catch (LocalFailure e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
