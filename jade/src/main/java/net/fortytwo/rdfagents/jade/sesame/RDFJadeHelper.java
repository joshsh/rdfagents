package net.fortytwo.rdfagents.jade.sesame;

import net.fortytwo.rdfagents.util.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 8:36 PM
 */
public class RDFJadeHelper {
    private static final RDFJadeHelper INSTANCE = new RDFJadeHelper();
    private static long ID_COUNT = 0;

    public static RDFJadeHelper getInstance() {
        return INSTANCE;
    }

    private final Map<Long, Configuration> confById;

    private RDFJadeHelper() {
        confById = new HashMap<Long, Configuration>();
    }

    public synchronized long registerConfiguration(final Configuration conf) {
        if (!confById.values().contains(conf)) {
            ID_COUNT++;
            confById.put(ID_COUNT, conf);
            return ID_COUNT;
        } else {
            throw new IllegalStateException("configuration is already registered");
        }
    }

    public synchronized Configuration findConfiguration(final long id) {
        return confById.get(id);
    }

    public synchronized void deregisterConfiguration(final long id) {
        confById.remove(id);
    }

    public Configuration.AgentProfile findAgentProfile(final String globalNickname) {
        int i = globalNickname.indexOf("-");
        if (0 >= i) {
            throw new IllegalArgumentException();
        }

        long confId = new Long(globalNickname.substring(0, i));
        String localNick = globalNickname.substring(i + 1);

        Configuration conf = findConfiguration(confId);
        if (null == conf) {
            throw new IllegalStateException();
        }

        Configuration.AgentProfile p = conf.getAgentProfiles().get(localNick);
        if (null == p) {
            throw new IllegalStateException();
        }

        return p;
    }
}
