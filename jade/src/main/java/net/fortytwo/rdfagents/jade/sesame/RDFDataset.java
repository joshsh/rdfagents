package net.fortytwo.rdfagents.jade.sesame;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * A (possibly streaming) set of RDF statement with context, treated as a single unit of data.
 *
 * User: josh
 * Date: 2/23/11
 * Time: 4:06 PM
 */
public class RDFDataset {
    private static final String BASE_URI = "http://example.org/baseURI#";

    private final RDFFormat format;
    private final InputStream content;
    private final Collection<Statement> statements;

    private boolean alreadyWritten = false;

    public RDFDataset(RDFFormat format,
                      InputStream content) {
        this.format = format;
        this.content = content;
        statements = null;
    }

    public RDFDataset(Collection<Statement> statements) {
        this.format = null;
        this.content = null;
        this.statements = statements;
    }

    /**
     * Outputs this RDF dataset as a collection of statements.
     *
     * @param handler a handler for the RDF statements in this document
     * @throws RDFWriteException if the RDF data is not successfully written
     */
    public void write(final RDFHandler handler) throws RDFWriteException {
        if (alreadyWritten) {
            throw new RDFWriteException("redundant write (RDFDatasets are once-only streams)");
        }

        alreadyWritten = true;

        try {
            if (null == format) {
                handler.startRDF();
                try {
                    for (Statement s : statements) {
                        handler.handleStatement(s);
                    }
                } finally {
                    handler.endRDF();
                }
            } else {
                content.reset();
                try {
                    RDFParser p = Rio.createParser(format);
                    p.setRDFHandler(handler);
                    p.parse(content, BASE_URI);
                } finally {
                    content.close();
                }
            }
        } catch (RDFHandlerException e) {
            throw new RDFWriteException(e);
        } catch (IOException e) {
            throw new RDFWriteException(e);
        } catch (RDFParseException e) {
            throw new RDFWriteException(e);
        }
    }

    /**
     * Serializes this RDF dataset to a stream in the given format.
     *
     * @param out    the destination of the RDF serialization
     * @param format the desired RDF serialization format
     * @throws RDFWriteException if the RDF data is not successfully written
     */
    public void write(final OutputStream out,
                      final RDFFormat format) throws RDFWriteException {

        RDFWriter writer = Rio.createWriter(format, out);
        write(writer);
    }
}
