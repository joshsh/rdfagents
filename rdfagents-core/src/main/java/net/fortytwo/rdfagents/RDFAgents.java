package net.fortytwo.rdfagents;

import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;

import java.util.logging.Logger;

/**
 * A collection of global constants for the RDFAgents messaging API.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFAgents {
    private static final Logger logger = Logger.getLogger(RDFAgents.class.getName());

    // Agent profile properties
    public static final String
            QUERY_ANSWERING_SUPPORTED = "behavior.query.answer.supported",
            QUERY_ASKING_SUPPORTED = "behavior.query.ask.supported";

    public static final String
            // TODO
            BASE_IRI = "http://example.org/",
            RDFAGENTS_ONTOLOGY_NAME = "rdfagents",
            RDFAGENTS_ACCEPT_PARAMETER = "X-rdfagents-accept";

    public static final String NAME_PREFIX = "urn:x-agent:";
    public static final String XMPP_URI_PREFIX = "xmpp://";

    public enum Protocol {
        Query("fipa-query"), Subscribe("fipa-subscribe");

        private final String fipaName;

        private Protocol(final String fipaName) {
            this.fipaName = fipaName;
        }

        public String getFipaName() {
            return fipaName;
        }

        public static Protocol getByName(final String name) {
            for (Protocol l : Protocol.values()) {
                if (l.fipaName.equals(name)) {
                    return l;
                }
            }

            return null;
        }
    }

    private RDFAgents() {
    }

    public static boolean isValidIRI(final String iri) {
        // TODO: make this more efficient, and base it on the IRI spec
        try {
            createIRI(iri);
            return true;
        } catch (Exception t) {
            return false;
        }
    }

    public static IRI createIRI(String iri) {
        return SimpleValueFactory.getInstance().createIRI(iri);
    }

    public static Literal createLiteral(String iri) {
        return SimpleValueFactory.getInstance().createLiteral(iri);
    }

    public static Statement createStatement(Resource subject, IRI predicate, Value object) {
        return SimpleValueFactory.getInstance().createStatement(subject, predicate, object);
    }
}
