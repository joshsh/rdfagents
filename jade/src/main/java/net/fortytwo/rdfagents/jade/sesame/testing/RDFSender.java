package net.fortytwo.rdfagents.jade.sesame.testing;

import net.fortytwo.rdfagents.jade.sesame.RDFDataset;
import net.fortytwo.rdfagents.messaging.push.Notification;
import net.fortytwo.rdfagents.messaging.push.Sender;
import net.fortytwo.rdfagents.model.Agent;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 3:02 PM
 */
public class RDFSender extends Sender<RDFDataset> {
    public RDFSender(Agent agent) {
        super(agent);
    }

    @Override
    public void send(Notification<RDFDataset> note) {

    }

    @Override
    public void send(Notification<RDFDataset> note,
                     NotificationCallback callback) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
