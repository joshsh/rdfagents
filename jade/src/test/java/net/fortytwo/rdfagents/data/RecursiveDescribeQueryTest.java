package net.fortytwo.rdfagents.data;

import net.fortytwo.rdfagents.RDFAgentsTestCase;
import net.fortytwo.rdfagents.model.Dataset;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 1:51 PM
 */
public class RecursiveDescribeQueryTest extends RDFAgentsTestCase {

    public void testAll() throws Exception {
        DatasetQuery q = new RecursiveDescribeQuery(ARTHUR, sail);
        Dataset arthur = q.evaluate();

        Dataset r = datasetFactory.receiveDataset(arthur, sender);
        showDataset(r);
    }
}
