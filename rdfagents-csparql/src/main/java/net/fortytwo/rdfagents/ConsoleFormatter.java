package net.fortytwo.rdfagents;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.common.streams.format.GenericObservable;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class ConsoleFormatter extends ResultFormatter {
    @Override
    public void update(final GenericObservable<RDFTable> observed, final RDFTable q) {

        System.out.println("------- results at SystemTime=[" + System.currentTimeMillis() + "]--------");
        for (final RDFTuple t : q) {
            System.out.println(t.toString());
        }

    }
}
