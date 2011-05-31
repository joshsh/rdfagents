package net.fortytwo.rdfagents;

import org.openrdf.model.impl.URIImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * A collection of global constants for the RDFAgents messaging API.
 */
public class RDFAgents {
    private static final Logger LOGGER = Logger.getLogger(RDFAgents.class.getName());

    // Agent profile properties
    public static final String
            QUERY_ANSWERING_SUPPORTED = "behavior.query.answer.supported",
            QUERY_ASKING_SUPPORTED = "behavior.query.ask.supported";

    public static final String
            // TODO
            BASE_URI = "http://example.org/",
            RDFAGENTS_ONTOLOGY_NAME = "rdfagents",
            RDFAGENTS_ACCEPT_PARAMETER = "X-rdfagents-accept",
            RANDOM_URN_PREFIX = "urn:random:";

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

    public static boolean isValidURI(final String uri) {
        // TODO: make this more efficient, and base it on the URI spec
        try {
            new URIImpl(uri);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String stackTraceToString(final Throwable t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            t.printStackTrace(new PrintStream(out));
            return out.toString();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LOGGER.severe("failed to serialize stack trace.  Secondary error is: " + e);
                e.printStackTrace(System.err);
            }
        }
    }
}
