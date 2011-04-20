package net.fortytwo.rdfagents.jade;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 5:26 PM
 */
public class SendingBehaviour extends OneShotBehaviour {
    private final ACLMessage messsage;

    public SendingBehaviour(ACLMessage messsage) {
        this.messsage = messsage;
    }

    @Override
    public void action() {
        myAgent.send(messsage);
    }
}
