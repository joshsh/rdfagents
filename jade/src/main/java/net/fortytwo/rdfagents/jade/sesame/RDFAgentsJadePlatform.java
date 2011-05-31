package net.fortytwo.rdfagents.jade.sesame;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.Logger;
import jade.util.leap.Properties;
import net.fortytwo.rdfagents.util.Configuration;

import java.io.IOException;
import java.util.Collection;

/**
 * An RDFAgent platform which uses JADE as its FIPA implementation and Sesame as its RDF framework.
 * <p/>
 * User: josh
 * Date: 2/23/11
 * Time: 7:08 PM
 */
public class RDFAgentsJadePlatform {
    private static final Logger LOGGER = Logger.getMyLogger(RDFAgentsJadePlatform.class.getName());

    private final Configuration config;
    private final long configId;

    /**
     * Instantiates and launches a concrete RDFAgent platform.
     *
     * @param config
     */
    public RDFAgentsJadePlatform(final Configuration config) {
        this.config = config;
        this.configId = RDFJadeHelper.getInstance().registerConfiguration(config);
    }

    public void launch() throws IOException {
        System.out.println("## " + jade.core.Runtime.getCopyrightNotice());

        //String agents = "timer:net.fortytwo.droidspeak.jade.TimerAgent;echo:net.fortytwo.droidspeak.jade.EchoAgent";

        Properties jadeProps = createJadeConfiguration(config);
        ProfileImpl profile = new ProfileImpl(jadeProps);

        // Start a new JADE runtime system
        Runtime.instance().setCloseVM(true);
        // Check whether this is the Main Container or a peripheral container
        if (profile.getBooleanProperty(Profile.MAIN, true)) {
            LOGGER.info("creating main container");
            Runtime.instance().createMainContainer(profile);
        } else {
            LOGGER.info("creating peripheral container");
            Runtime.instance().createAgentContainer(profile);
        }
    }

    public void shutDown() {
        // TODO ...

        RDFJadeHelper.getInstance().deregisterConfiguration(configId);
    }

    public Properties createJadeConfiguration(final Configuration config) throws IOException {
        Properties props = new ExtendedProperties();

        boolean container = false, backupmain = false, gui = false, nomtp = false;
        String platformName = null, conf = null;

        if (container) {
            props.setProperty(Profile.MAIN, "false");
        }

        if (backupmain) {
            props.setProperty(Profile.LOCAL_SERVICE_MANAGER, "true");
        }

        if (gui) {
            props.setProperty(Profile.GUI, "true");
        }

        if (nomtp) {
            props.setProperty(Profile.NO_MTP, "true");
        }

        if (null != platformName) {
            props.setProperty(Profile.PLATFORM_ID, platformName);
        }

        String mtps = "net.fortytwo.droidspeak.xmpp.XmppMessageTransportProtocol";
        props.setProperty(Profile.MTPS, mtps);

        if (null != conf) {
            props.load(conf);
        }

        java.util.Properties global = config.getGlobalProperties();
        for (String key : global.stringPropertyNames()) {
            props.setProperty(key, global.getProperty(key));
        }

        Collection<Configuration.AgentProfile> profiles = config.getAgentProfiles().values();
        if (0 < profiles.size()) {
            if (null != props.getProperty(Profile.AGENTS)) {
                LOGGER.warning("overriding agents specification set with the \"-agents\" option");
            }

            props.setProperty(Profile.AGENTS, agentPropertyValue(profiles));
        } else {
            LOGGER.warning("no agent profiles provided");
        }

        // Consistency check
        if ("true".equals(props.getProperty(Profile.NO_MTP))
                && props.getProperty(Profile.MTPS) != null) {
            System.err.println("WARNING: both \"-mtps\" and \"-nomtp\" options specified. The latter will be ignored");
            props.remove(Profile.NO_MTP);
        }

        return props;
    }

    private String agentPropertyValue(final Collection<Configuration.AgentProfile> profiles) {
        StringBuilder sb = new StringBuilder();

        // They're all RDFAgents.
        String className = RDFAgent.class.getName();

        boolean first = true;
        for (Configuration.AgentProfile a : profiles) {
            if (first) {
                first = false;
            } else {
                sb.append(";");
            }
            String globalNick = configId + "-" + a.getNickname();
            sb.append(globalNick).append(":").append(className);
        }

        LOGGER.fine("agents: " + sb.toString());
        return sb.toString();
    }
}
