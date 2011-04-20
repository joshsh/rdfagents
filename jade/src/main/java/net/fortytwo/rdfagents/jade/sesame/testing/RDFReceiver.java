package net.fortytwo.rdfagents.jade.sesame.testing;

import net.fortytwo.rdfagents.jade.sesame.RDFDataset;
import net.fortytwo.rdfagents.jade.sesame.RDFWriteException;
import net.fortytwo.rdfagents.messaging.push.Notification;
import net.fortytwo.rdfagents.messaging.push.Receiver;
import net.fortytwo.rdfagents.model.Agent;
import org.openrdf.rio.RDFFormat;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 3:02 PM
 */
public class RDFReceiver extends Receiver<RDFDataset> {
    public RDFReceiver(final Agent agent) {
        super(agent);
    }

    @Override
    public void receive(final Notification<RDFDataset> note) {
        System.out.println("received RDF dataset from " + note.getSender().getId() + ":");
        try {
            note.getContent().write(System.out, RDFFormat.NTRIPLES);
        } catch (RDFWriteException e) {
            e.printStackTrace(System.err);
        }
    }
}
