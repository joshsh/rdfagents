package net.fortytwo.rdfagents.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Specifier;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import net.fortytwo.rdfagents.model.RDFAgent;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;
import net.fortytwo.rdfagents.data.DatasetFactory;

import java.util.Properties;

/**
 * User: josh
 * Date: 5/27/11
 * Time: 10:17 PM
 */
public class RDFAgentsPlatformImpl extends RDFAgentsPlatform {
    private static final String MTPS = "mtps";

    private static final String
            XMPP_MTP_SERVER = "jade_mtp_xmpp_server",
            XMPP_MTP_USERNAME = "jade_mtp_xmpp_username",
            XMPP_MTP_PASSWORD = "jade_mtp_xmpp_passwd";

    private final AgentContainer container;

    // TODO: support attaching RDFAgents to an existing container
    public RDFAgentsPlatformImpl(final String name,
                                 final DatasetFactory datasetFactory,
                                 final int port,
                                 final Properties config) throws Exception {
        super(name, datasetFactory);

        // Get a hold on JADE runtime
        jade.core.Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);

        // Launch a complete platform on the 8888 port
        // create a default Profile
        Profile p = new ProfileImpl(null, port, name);
        p.setParameter(XMPP_MTP_SERVER, config.getProperty(XMPP_MTP_SERVER));
        p.setParameter(XMPP_MTP_USERNAME, config.getProperty(XMPP_MTP_USERNAME));
        p.setParameter(XMPP_MTP_PASSWORD, config.getProperty(XMPP_MTP_PASSWORD));
        //   p.setParameter(MTPS, jade.mtp.xmpp.MessageTransportProtocol.class.getName());

        //*
        List mtps = new LinkedList();
        Specifier xmpp = new Specifier();
        //xmpp.setName("XMPP MTP [JJS was here]");
        xmpp.setClassName(jade.mtp.xmpp.MessageTransportProtocol.class.getName());
        mtps.add(xmpp);
        p.setSpecifiers(MTPS, mtps);
        //*/

        System.out.println("Launching JADE container for RDFAgents: " + p);
        container = rt.createMainContainer(p);
    }

    public RDFAgent createAgent(final String localName,
                                final String... addresses) throws RDFAgent.RDFAgentException {
        RDFAgentImpl agent = new RDFAgentImpl(localName, this, addresses);

        MessageFactory messageFactory = new MessageFactory(datasetFactory);
        RDFJadeAgent.Wrapper w = new RDFJadeAgent.Wrapper(agent.getIdentity(), messageFactory);

        try {
            CondVar startUpLatch = new CondVar();

            AgentController c = container.createNewAgent(localName, RDFJadeAgent.class.getName(),
                    new Object[]{startUpLatch, w});
            c.start();

            // Wait until the agent starts up and notifies the Object
            startUpLatch.waitOn();

            agent.setController(c);
        } catch (StaleProxyException e) {
            throw new RDFAgent.RDFAgentException(e);
        } catch (InterruptedException e) {
            throw new RDFAgent.RDFAgentException(e);
        }

        agent.setJadeAgent(w.getJadeAgent());

        return agent;
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
