package net.fortytwo.rdfagents.data;

import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.model.AgentId;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.RDFContentLanguage;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DatasetFactory {
    private static final String
            FOAF_NS = "http://xmlns.com/foaf/0.1/",
            RDFG_NS = "http://www.w3.org/2004/03/trix/rdfg-1/",
            SWP_NS = "http://www.w3.org/2004/03/trix/swp-2/";

    private static final URI
            FOAF_AGENT = new URIImpl(FOAF_NS + "Agent"),
            FOAF_MBOX = new URIImpl(FOAF_NS + "mbox"),
            RDFG_GRAPH = new URIImpl(RDFG_NS + "Graph"),
            SWP_ASSERTEDBY = new URIImpl(SWP_NS + "assertedBy"),
            SWP_AUTHORITY = new URIImpl(SWP_NS + "authority");

    private static final String UUID_URN_PREFIX = "urn:uuid:";

    private final ValueFactory valueFactory;
    private final Random random = new Random();
    private final Set<RDFContentLanguage> supportedLanguages;

    public DatasetFactory(final ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        supportedLanguages = new HashSet<RDFContentLanguage>();
    }

    public DatasetFactory() {
        this(new ValueFactoryImpl());

        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            addLanguage(l);
        }
    }

    public void addLanguage(final RDFContentLanguage language) {
        supportedLanguages.add(language);
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    public Set<RDFContentLanguage> getSupportedLanguages() {
        return Collections.unmodifiableSet(supportedLanguages);
    }

    /**
     * Transforms the sender's dataset of an assertional message to a corresponding receiver's dataset.
     * See: http://fortytwo.net/2011/rdfagents/spec#asserting-graphs
     * So as to avoid collision of graph names,
     * each graph in the sender's dataset is given a new name in the receiver's dataset.
     *
     * @param dataset the sender's dataset to transform
     * @param sender  the agent sending the message
     * @return the corresponding receiver's dataset
     */
    public Dataset receiveDataset(final Dataset dataset,
                                  final AgentId sender) {
        /*
        System.out.println("sender's dataset:");
        try {
            write(System.out, dataset, RDFContentLanguage.RDF_TRIG);
        } catch (LocalFailure localFailure) {
            localFailure.printStackTrace();
        }
        //*/

        URI formerDefaultGraph = randomURI();
        Collection<Statement> receiverStatements = new LinkedList<Statement>();

        for (Statement s : dataset.getStatements()) {
            // The default graph is renamed
            if (null == s.getContext()) {
                receiverStatements.add(valueFactory.createStatement(
                        s.getSubject(), s.getPredicate(), s.getObject(), formerDefaultGraph));
            } else {
                receiverStatements.add(s);
            }
        }

        // Metadata about the sender and informative act is added to the receiver's default graph.
        receiverStatements.add(valueFactory.createStatement(sender.getUri(), RDF.TYPE, FOAF_AGENT));
        for (URI address : sender.getTransportAddresses()) {
            receiverStatements.add(valueFactory.createStatement(sender.getUri(), FOAF_MBOX, address));
        }
        receiverStatements.add(valueFactory.createStatement(formerDefaultGraph, RDF.TYPE, RDFG_GRAPH));
        receiverStatements.add(valueFactory.createStatement(formerDefaultGraph, SWP_ASSERTEDBY, formerDefaultGraph));
        receiverStatements.add(valueFactory.createStatement(formerDefaultGraph, SWP_AUTHORITY, sender.getUri()));

        return renameGraphs(new Dataset(receiverStatements));
    }

    public void addToSail(final Dataset dataset,
                          final Sail sail) throws SailException {
        SailConnection sc = sail.getConnection();
        try {
            sc.begin();

            for (Statement s : dataset.getStatements()) {
                sc.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext());
            }
            sc.commit();
        } finally {
            sc.rollback();
            sc.close();
        }
    }

    /**
     * @param original an RDF Dataset to transform
     * @return a new RDF Dataset in which all named graphs in the original dataset have been renamed.
     * This involves changing the names of the graphs in the dataset,
     * as well as modifying all statements in the dataset which reference those graphs.
     */
    public Dataset renameGraphs(final Dataset original) {
        Set<Resource> graphs = new HashSet<Resource>();
        for (Statement s : original.getStatements()) {
            Resource g = s.getContext();
            if (null != g) {
                graphs.add(g);
            }
        }

        Map<Value, URI> newNames = new HashMap<Value, URI>();

        for (Resource g : graphs) {
            newNames.put(g, randomURI());
        }

        Collection<Statement> coll = new LinkedList<Statement>();
        for (Statement s : original.getStatements()) {
            Statement n = valueFactory.createStatement((Resource) rename(s.getSubject(), newNames),
                    (URI) rename(s.getPredicate(), newNames),
                    rename(s.getObject(), newNames),
                    (Resource) rename(s.getContext(), newNames));
            coll.add(n);
        }

        return new Dataset(coll);
    }

    public Dataset parse(final InputStream in,
                         final RDFContentLanguage language) throws InvalidRDFContentException, LocalFailure {
        if (!supportedLanguages.contains(language)) {
            throw new LocalFailure("unsupported RDF content language: " + language);
        }

        RDFParser p;
        switch (language) {
            case RDF_NTRIPLES:
                p = Rio.createParser(language.getFormat());
                break;
            case RDF_TURTLE:
                p = Rio.createParser(language.getFormat());
                break;
            case RDF_N3:
                p = Rio.createParser(language.getFormat());
                break;

            case RDF_XML:
                p = Rio.createParser(language.getFormat());
                break;
            case RDF_NQUADS:
                p = Rio.createParser(language.getFormat());
                break;
            case RDF_TRIG:
                p = Rio.createParser(language.getFormat());
                break;
            case RDF_TRIX:
                p = Rio.createParser(language.getFormat());
                break;
            default:
                throw new LocalFailure("unexpected content language: " + language);
        }

        DatasetCreator d = new DatasetCreator();
        p.setRDFHandler(d);
        try {
            p.parse(in, RDFAgents.BASE_URI);
        } catch (IOException e) {
            throw new LocalFailure(e);
        } catch (RDFParseException e) {
            throw new InvalidRDFContentException(e);
        } catch (RDFHandlerException e) {
            throw new InvalidRDFContentException(e);
        }

        return d.getDataset();
    }

    /**
     * Writes a dataset to an output stream using the given RDF content language.
     *
     * @param out      the output stream to which to write the dataset
     * @param dataset  the dataset to write
     * @param language the language in which to encode the dataset
     * @throws LocalFailure if writing fails
     */
    public void write(final OutputStream out,
                      final Dataset dataset,
                      final RDFContentLanguage language) throws LocalFailure {
        RDFWriter w;

        switch (language) {
            case RDF_NTRIPLES:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_TURTLE:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_N3:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_JSON:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_XML:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_NQUADS:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_TRIG:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            case RDF_TRIX:
                w = Rio.createWriter(language.getFormat(), out);
                break;
            default:
                throw new LocalFailure("unexpected content language: " + language);
        }

        try {
            w.startRDF();

            for (Statement s : dataset.getStatements()) {
                w.handleStatement(s);
            }

            w.endRDF();
        } catch (RDFHandlerException e) {
            throw new LocalFailure(e);
        }
    }

    private Value rename(final Value original,
                         final Map<Value, URI> newNames) {
        if (null == original) {
            return null;
        }

        URI n = newNames.get(original);
        return null == n ? original : n;
    }

    public URI randomURI() {
        return valueFactory.createURI(UUID_URN_PREFIX + UUID.randomUUID());
    }

    private class DatasetCreator implements RDFHandler {
        private final Dataset dataset;
        private final Collection<Statement> statements;

        public DatasetCreator() {
            statements = new LinkedList<Statement>();
            dataset = new Dataset(statements);
        }

        public void startRDF() throws RDFHandlerException {
        }

        public void endRDF() throws RDFHandlerException {
        }

        public void handleNamespace(String s, String s1) throws RDFHandlerException {
        }

        public void handleStatement(final Statement s) throws RDFHandlerException {
            statements.add(s);
        }

        public void handleComment(String s) throws RDFHandlerException {
        }

        public Dataset getDataset() {
            return dataset;
        }
    }

    public static class InvalidRDFContentException extends Exception {
        public InvalidRDFContentException(final Throwable cause) {
            super(cause);
        }
    }
}
