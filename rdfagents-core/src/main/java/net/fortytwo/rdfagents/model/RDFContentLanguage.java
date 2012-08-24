package net.fortytwo.rdfagents.model;

import org.openrdf.rio.RDFFormat;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public enum RDFContentLanguage {
    RDF_NTRIPLES("rdf-ntriples", RDFFormat.NTRIPLES),
    RDF_TURTLE("rdf-turtle", RDFFormat.TURTLE),
    RDF_N3("rdf-n3", RDFFormat.N3),
    RDF_XML("rdf-xml", RDFFormat.RDFXML),
    RDF_NQUADS("rdf-nquads", RDFFormat.NQUADS),
    RDF_TRIG("rdf-trig", RDFFormat.TRIG),
    RDF_TRIX("rdf-trix", RDFFormat.TRIX);

    private final String fipaName;
    private final RDFFormat format;

    private RDFContentLanguage(final String fipaName,
                               final RDFFormat format) {
        this.fipaName = fipaName;
        this.format = format;
    }

    public String getFipaName() {
        return fipaName;
    }

    public RDFFormat getFormat() {
        return format;
    }

    public static RDFContentLanguage getByName(final String name) {
        for (RDFContentLanguage l : RDFContentLanguage.values()) {
            if (l.fipaName.equals(name)) {
                return l;
            }
        }

        return null;
    }
}
