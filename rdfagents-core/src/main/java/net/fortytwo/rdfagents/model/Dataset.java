package net.fortytwo.rdfagents.model;

import org.openrdf.model.Statement;

import java.util.Collection;

/**
 * An RDF Dataset (as defined in the SPARQL specification) containing a set of zero or more named graphs
 * and exactly one unnamed graph, suitable for embedding in FIPA messages.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Dataset {
    public static final String UUID_URN_PREFIX = "urn:uuid:";

    private final Collection<Statement> statements;

    public Dataset(Collection<Statement> statements) {
        this.statements = statements;
    }

    public Collection<Statement> getStatements() {
        return statements;
    }
}
