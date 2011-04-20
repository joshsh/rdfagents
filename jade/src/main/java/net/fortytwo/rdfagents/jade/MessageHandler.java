package net.fortytwo.rdfagents.jade;

import jade.lang.acl.ACLMessage;

/**
 * User: josh
 * Date: 3/11/11
 * Time: 5:22 PM
 */
public interface MessageHandler {
    void handle(ACLMessage message);
}
