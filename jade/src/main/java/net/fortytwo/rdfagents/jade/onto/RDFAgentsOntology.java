package net.fortytwo.rdfagents.jade.onto;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import jade.content.onto.BasicOntology;
import jade.content.onto.Introspector;
import jade.content.onto.Ontology;

/**
 * User: josh
 * Date: 4/23/11
 * Time: 2:09 PM
 */
public class RDFAgentsOntology extends Ontology {
    private static final RDFAgentsOntology INSTANCE = new RDFAgentsOntology();

    private RDFAgentsOntology() {
        super("rdfagents", BasicOntology.getInstance());
    }

    public static RDFAgentsOntology getInstance() {
        return INSTANCE;
    }
}
