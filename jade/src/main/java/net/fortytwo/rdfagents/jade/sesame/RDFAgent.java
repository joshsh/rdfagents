package net.fortytwo.rdfagents.jade.sesame;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import net.fortytwo.rdfagents.util.Configuration;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.jade.RDFJadeHelper;
import net.fortytwo.rdfagents.jade.sesame.behaviors.QueryAnsweringBehavior;
import net.fortytwo.rdfagents.util.properties.PropertyException;
import net.fortytwo.rdfagents.util.properties.TypedProperties;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 4:15 PM
 */
public class RDFAgent extends Agent {
    private static final Logger LOGGER = Logger.getLogger(RDFAgent.class.getName());

    private final String name;
    private final Configuration.AgentProfile profile;

    public RDFAgent(String name) {
        System.out.println("## instantiating RDFAgent");

        if (null == name || 0 == name.length()) {
            throw new IllegalArgumentException("null or empty agent name");
        }
        this.name = name;

        profile = RDFJadeHelper.getInstance().findAgentProfile(name);
        try {
            configure();
        } catch (PropertyException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private void configure() throws PropertyException {
        TypedProperties props = profile.getParameters();

        if (props.getBoolean(RDFAgents.QUERY_ANSWERING_SUPPORTED, false)) {
            addBehaviour(new QueryAnsweringBehavior());
        }
    }

    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("RDFAgent");
        sd.setName(name);

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("## The new RDFAgent is available here:");
        Iterator i = this.getAID().getAllAddresses();
        while (i.hasNext()) {
            System.out.println("\t" + i.next());
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
