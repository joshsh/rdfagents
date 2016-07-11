package net.fortytwo.rdfagents.jade.testing;

import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFContentLanguage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class EchoCallback implements ConsumerCallback<Dataset> {
    private static final Logger logger = Logger.getLogger(EchoCallback.class.getName());

    private final DatasetFactory datasetFactory;

    public EchoCallback(DatasetFactory datasetFactory) {
        this.datasetFactory = datasetFactory;
    }

    public void success(final Dataset answer) {
        System.out.println("query result or update received:");
        try {
            datasetFactory.write(System.out, answer, RDFContentLanguage.RDF_TRIG);
        } catch (LocalFailure e) {
            e.printStackTrace(System.err);
        }
    }

    public void agreed() {
        logger.info("agreed!");
    }

    public void refused(final ErrorExplanation explanation) {
        logger.warning("refused!");
    }

    public void remoteFailure(final ErrorExplanation explanation) {
        logger.warning("remote failure: " + explanation);
    }

    public void localFailure(final LocalFailure e) {
        logger.log(Level.WARNING, "local failure", e);
    }
}
