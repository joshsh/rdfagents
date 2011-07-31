package net.fortytwo.rdfagents;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.common.streams.format.GenericObservable;
import eu.larkc.csparql.common.streams.format.GenericObserver;
import net.fortytwo.rdfagents.messaging.Commitment;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.messaging.subscribe.PubsubProviderTmp;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFAgent;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import java.util.ArrayList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CSparqlPubsubProvider extends PubsubProviderTmp<Value, Dataset> implements GenericObserver<RDFTable> {

	Value topic;
	
	public CSparqlPubsubProvider(RDFAgent agent, Value topic) {
		super(agent);
		this.topic=topic;
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
		
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		for (RDFTuple t : q) {
			String subject = t.get(0);
			String property  = t.get(1);
			String object  = t.get(2);
//			String[] objectParts = object.split("\\^\\^");
//			if (objectParts.length>1) {
//				LiteralImpl l = new LiteralImpl(objectParts[0].replaceAll("\"", ""), new URIImpl(objectParts[1]));
//				statements.add(new StatementImpl(new URIImpl(subject),
//						new URIImpl(property), l ));
//				
//			} else {
				if (object.contains("\"")) {
					LiteralImpl l = new LiteralImpl(object.replaceAll("\"", ""));
					statements.add(new StatementImpl(new URIImpl(subject),
							new URIImpl(property), l ));
					}
				else {
				
				statements.add(new StatementImpl(new URIImpl(subject),
						new URIImpl(property), new URIImpl(object)));
				}
//			}
			
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
