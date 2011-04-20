package net.fortytwo.rdfagents.jade.sesame.testing;

import net.fortytwo.rdfagents.jade.sesame.RDFDataset;

import java.net.URI;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 6:15 PM
 */
public class PubsubService {

    /**
     * Publish a delta against a specific URI...
     *
     * @param uri
     * @param delta
     */
    public void publish(final URI uri,
                        final RDFDataset delta) {

    }

    /**
     * Lets the implementation decide which slice of the delta to publish against which URI...
     *
     * @param delta
     */
    public void publish(final RDFDataset delta) {

    }

    // policies...
}
