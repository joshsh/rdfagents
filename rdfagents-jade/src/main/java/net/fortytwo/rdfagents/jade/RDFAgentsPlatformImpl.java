package net.fortytwo.rdfagents.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.Specifier;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.wrapper.AgentContainer;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.model.RDFAgentsPlatform;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFAgentsPlatformImpl extends RDFAgentsPlatform {
    private static final String MTPS = "mtps";

    private static final String
            XMPP_MTP_SERVER = "jade_mtp_xmpp_server",
            XMPP_MTP_USERNAME = "jade_mtp_xmpp_username",
            XMPP_MTP_PASSWORD = "jade_mtp_xmpp_passwd";

    private final AgentContainer container;
    private final Runtime runtime;

    // TODO: support attaching RDFAgents to an existing container
    public RDFAgentsPlatformImpl(final String name,
                                 final DatasetFactory datasetFactory,
                                 final int port,
                                 final Properties config) {
        super(name, datasetFactory);

        // Get a hold on JADE runtime
        runtime = Runtime.instance();

        // Exit the JVM when there are no more containers around
        runtime.setCloseVM(true);

        // Launch a complete platform on the 8888 port
        // create a default Profile
        Profile p = new ProfileImpl(null, port, name);
        List mtps = new LinkedList();

        /*
        // start an HTTP MTP by default
        Specifier http = new Specifier();
        http.setClassName(jade.mtp.http.MessageTransportProtocol.class.getName());
        mtps.add(http);
        */

        // start an XMPP MTP if the configuration is provided
        if (null != config.get(XMPP_MTP_SERVER)) {
            p.setParameter(XMPP_MTP_SERVER, config.getProperty(XMPP_MTP_SERVER));
            p.setParameter(XMPP_MTP_USERNAME, config.getProperty(XMPP_MTP_USERNAME));
            p.setParameter(XMPP_MTP_PASSWORD, config.getProperty(XMPP_MTP_PASSWORD));
            //   p.setParameter(MTPS, jade.mtp.xmpp.MessageTransportProtocol.class.getName());
            Specifier xmpp = new Specifier();
            xmpp.setClassName(jade.mtp.xmpp.MessageTransportProtocol.class.getName());
            mtps.add(xmpp);
        }

        p.setSpecifiers(MTPS, mtps);

        System.out.println("Launching JADE container for RDFAgents: " + p);
        container = runtime.createMainContainer(p);
    }


    public RDFAgentsPlatformImpl(final String name,
                                 final int port,
                                 final Properties config) throws IOException {
        this(name, new DatasetFactory(), port, config);
    }

    public AgentContainer getContainer() {
        return container;
    }

    @Override
    public void shutDown() {
        runtime.shutDown();
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
