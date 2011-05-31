package net.fortytwo.rdfagents.model;

import org.openrdf.model.Statement;

import java.util.Collection;

/**
 * An RDF Dataset (as defined in the SPARQL specification) containing a set of zero or more named graphs
 * and exactly one unnamed graph, suitable for embedding in FIPA messages.
 * <p/>
 * User: josh
 * Date: 5/23/11
 * Time: 5:46 PM
 */
public class Dataset {
    private final Collection<Statement> statements;

    public Dataset(Collection<Statement> statements) {
        this.statements = statements;
    }

    public Collection<Statement> getStatements() {
        return statements;
    }
}
