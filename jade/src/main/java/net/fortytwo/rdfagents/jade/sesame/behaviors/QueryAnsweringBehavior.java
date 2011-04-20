package net.fortytwo.rdfagents.jade.sesame.behaviors;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 4:17 PM
 */
public class QueryAnsweringBehavior extends CyclicBehaviour {
    public void action() {
        ACLMessage m = myAgent.receive();
        if (null != m) {
            System.out.println("## answering query: " + m);
            ACLMessage r = m.createReply();
            r.setLanguage(m.getLanguage());
            r.setContent(m.getContent());
            myAgent.send(r);
        } else {
            block();
        }
    }
}
