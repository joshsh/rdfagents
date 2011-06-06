package net.fortytwo.rdfagents.jade.testing;

import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.messaging.ConsumerCallback;
import net.fortytwo.rdfagents.messaging.LocalFailure;
import net.fortytwo.rdfagents.model.Dataset;
import net.fortytwo.rdfagents.model.ErrorExplanation;
import net.fortytwo.rdfagents.model.RDFContentLanguage;

/**
 * User: josh
 * Date: 6/5/11
 * Time: 6:06 PM
 */
public class EchoCallback implements ConsumerCallback<Dataset> {
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
        System.out.println("agreed!");
    }

    public void refused(final ErrorExplanation explanation) {
        System.out.println("refused!");
    }

    public void remoteFailure(final ErrorExplanation explanation) {
        System.out.println("remote failure: " + explanation);
    }

    public void localFailure(final LocalFailure e) {
        System.out.println("local failure: " + e + "\n" + RDFAgents.stackTraceToString(e));
    }
}
