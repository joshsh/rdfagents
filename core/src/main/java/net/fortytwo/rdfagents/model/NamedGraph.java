package net.fortytwo.rdfagents.model;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import java.util.Collection;

/**
 * A graph of RDF statements together with a URI serving as a graph name.
 * <p/>
 * User: josh
 * Date: 5/23/11
 * Time: 5:50 PM
 */
public class NamedGraph {
    private final URI name;
    private final Collection<Statement> statements;

    /**
     * @param name       the name of the Named Graph, or null for an unnamed graph
     * @param statements the statements of the graph
     */
    public NamedGraph(final URI name,
                      final Collection<Statement> statements) {
        this.name = name;
        this.statements = statements;
    }

    /**
     * @return the name of this graph, or null for an unnamed graph
     */
    public URI getName() {
        return name;
    }

    /**
     * @return all statements of the contained graph
     */
    public Collection<Statement> getStatements() {
        return statements;
    }
}
