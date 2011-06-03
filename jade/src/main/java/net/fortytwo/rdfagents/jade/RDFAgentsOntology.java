package net.fortytwo.rdfagents.jade;

import jade.content.lang.sl.SLOntology;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.model.ErrorExplanation;

import java.util.UUID;

/**
 * User: josh
 * Date: 4/23/11
 * Time: 2:09 PM
 */
public class RDFAgentsOntology extends Ontology {
    private static final RDFAgentsOntology INSTANCE;

    static {
        try {
            INSTANCE = new RDFAgentsOntology();
        } catch (OntologyException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static final String
            DATASET = "dataset",             // implicit (used only as a variable)
            DESCRIBES = "describes",
            DESCRIBES_DATASET = "dataset",   // implicit (unnamed parameter)
            DESCRIBES_SUBJECT = "subject",   // implicit (unnamed parameter)
            EXPLANATION = "explanation",     // implicit (abstract class) -- specific errors are in the ErrorExplanation enum
            EXPLANATION_MESSAGE = "message",
            LITERAL = "literal",
            LITERAL_DATATYPE = "datatype",
            LITERAL_LANGUAGE = "language",
            LITERAL_LABEL = "label",
            VALUE = "value",                 // implicit (abstract class)
            RESOURCE = "resource",
            RESOURCE_URI = "uri";

    private RDFAgentsOntology() throws OntologyException {
        super(RDFAgents.RDFAGENTS_ONTOLOGY_NAME, SLOntology.getInstance());

        add(new PredicateSchema(DESCRIBES));

        add(new ConceptSchema(DATASET));
        add(new ConceptSchema(VALUE));
        add(new ConceptSchema(RESOURCE));
        add(new ConceptSchema(LITERAL));

        add(new PredicateSchema(EXPLANATION));
        for (ErrorExplanation.Type x : ErrorExplanation.Type.values()) {
            add(new PredicateSchema(x.getFipaName()));
        }

        ConceptSchema dataset = (ConceptSchema) getSchema(DATASET);
        ConceptSchema resource = (ConceptSchema) getSchema(VALUE);
        ConceptSchema uri = (ConceptSchema) getSchema(RESOURCE);
        ConceptSchema literal = (ConceptSchema) getSchema(LITERAL);
        uri.addSuperSchema(resource);
        //uri.setEncodingByOrder(true);
        uri.add(RESOURCE_URI, (PrimitiveSchema) getSchema(BasicOntology.STRING));
        literal.addSuperSchema(resource);
        //literal.setEncodingByOrder(true);
        literal.add(LITERAL_LABEL, (PrimitiveSchema) getSchema(BasicOntology.STRING));
        literal.add(LITERAL_DATATYPE, uri, ObjectSchema.OPTIONAL);
        literal.add(LITERAL_LANGUAGE, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

        PredicateSchema describes = (PredicateSchema) getSchema(DESCRIBES);
        describes.add(DESCRIBES_DATASET, dataset);
        describes.add(DESCRIBES_SUBJECT, resource);

        PredicateSchema explanation = (PredicateSchema) getSchema(EXPLANATION);
        explanation.add(EXPLANATION_MESSAGE, getSchema(BasicOntology.STRING));

        for (ErrorExplanation.Type x : ErrorExplanation.Type.values()) {
            PredicateSchema s = (PredicateSchema) getSchema(x.getFipaName());
            s.addSuperSchema(explanation);
        }
    }

    public static RDFAgentsOntology getInstance() {
        return INSTANCE;
    }

    public static void main(final String[] args) throws Exception {

        System.out.println("" + UUID.nameUUIDFromBytes("foo".getBytes()));
        System.out.println("" + UUID.nameUUIDFromBytes("bar".getBytes()));
        System.out.println("" + UUID.nameUUIDFromBytes("foo".getBytes()));

        //  "((any ?dataset (describes ?dataset (uri http://example.org))))"

        // not legal
        //   "((all (describe (uri http://example.org))))"

        //    (literal "one two three")
        //    (literal :label "one two three")
        //    (literal :label "one two three" :language "en")
        //
        //    (uri "http://example.org")
        //    (uri :ref "http://example.org")

        //  (( any ?dataset (describes ?dataset (uri http://example.org))))
        //  (( any ?dataset (describes ?dataset (resource :uri http://example.org))))

        //  ( any ?dataset (describes ?dataset (literal :label "forty-two"))))
        //  ( any ?dataset (describes ?dataset (literal "forty-two" en))))
        //  ( any ?dataset (describes ?dataset (literal "forty-two" (uri http://xmlschema.org/types#string)))))


        //AbsConcept absCd = new AbsConcept(CD);
        //absCd.set(CD_NAME, "Synchronicity");



        //  (resource :uri http://example.org)

        // (resource :ref http://example.org)

        // (uri :ref http://example.org)


        //  (resource :literal "one two three" :language en)

        // (literal :value "one two three")
    }

}
