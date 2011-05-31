package net.fortytwo.rdfagents.jade.sesame;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 5:20 PM
 */
public class ReceivingBehavior extends CyclicBehaviour {
    private final MessageHandler handler;

    public ReceivingBehavior(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void action() {
        ACLMessage m = myAgent.receive();
        if (null != m) {
            System.out.println("## received message: " + m);
            handler.handle(m);
        } else {
            block();
        }
    }
}
