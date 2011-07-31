package net.fortytwo.rdfagents;

import org.openrdf.model.Statement;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CSPARQLConsumer extends RdfStream implements ConsumerCallback<Dataset> {

	public CSPARQLConsumer(String iri) {
		super(iri);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void agreed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void localFailure(LocalFailure arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refused(ErrorExplanation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remoteFailure(ErrorExplanation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void success(Dataset d) {
        System.out.println("*******=====***===*=");
		for (Statement st : d.getStatements()) {
			 final RdfQuadruple q = new RdfQuadruple(st.getSubject().toString(),
					 st.getPredicate().toString(), st.getObject().toString(), 
	                 System.nanoTime());
	         this.put(q);
		}
	}
	

}
