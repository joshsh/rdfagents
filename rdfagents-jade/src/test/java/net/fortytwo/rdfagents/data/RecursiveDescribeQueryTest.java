package net.fortytwo.rdfagents.data;

import net.fortytwo.rdfagents.RDFAgentsTestCase;
import net.fortytwo.rdfagents.model.Dataset;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RecursiveDescribeQueryTest extends RDFAgentsTestCase {

    public void testAll() throws Exception {
        DatasetQuery q = new RecursiveDescribeQuery(ARTHUR, sail);
        Dataset arthur = q.evaluate();

        Dataset r = datasetFactory.receiveDataset(arthur, sender);
        showDataset(r);
    }
}
