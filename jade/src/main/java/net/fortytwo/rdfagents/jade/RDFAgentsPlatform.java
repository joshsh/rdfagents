package net.fortytwo.rdfagents.jade;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import net.fortytwo.rdfagents.model.AgentReference;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 10:17 PM
 */
public class RDFAgentsPlatform {
    private final AgentContainer container;

    // TODO: support attaching RDFAgents to an existing container
    public RDFAgentsPlatform(final String platformId) throws Exception {
        // Get a hold on JADE runtime
        jade.core.Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);

        // Launch a complete platform on the 8888 port
        // create a default Profile
        Profile pMain = new ProfileImpl(null, 8888, platformId);

        System.out.println("Launching JADE container for RDFAgents: " + pMain);
        container = rt.createMainContainer(pMain);
    }

    public AgentController addAgent(final String nickname,
                                    final RDFAgent.Wrapper wrapper) throws Exception {
        CondVar startUpLatch = new CondVar();

        AgentController c = container.createNewAgent(nickname, RDFAgent.class.getName(),
                new Object[]{startUpLatch, wrapper});
        c.start();

        // Wait until the agent starts up and notifies the Object
        startUpLatch.waitOn();

        return c;
    }

    // Simple class behaving as a Condition Variable
    public static class CondVar {
        private boolean value = false;

        synchronized void waitOn() throws InterruptedException {
            while (!value) {
                wait();
            }
        }

        synchronized void signal() {
            value = true;
            notifyAll();
        }

    }
}
