package net.fortytwo.rdfagents.util;

import net.fortytwo.rdfagents.util.properties.TypedProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A key-value configuration which stores agent profiles and global configuration properties.
 * Global configuration properties are distinguished by their use of top-level domains.
 * For example, "org.example.myProperty" is the name of a global property, as it begins with the top-level DNS name "org".
 * Agent profiles, on the other hand, use property names which begin with an agent nickname (any alphanumeric sequence which is not a TLD).
 * For example "agent1.identity.xmpp.name" and "agent1.identity.xmpp.password" describe the XMPP credentials of the agent identified by "agent1".
 * <p/>
 * Note: internationalized country code TLDs are currently not included, but are not recommended as agent IDs.
 * <p/>
 * User: josh
 * Date: 2/23/11
 * Time: 5:35 PM
 */
public class Configuration {
    // These lists of TLDs were retrieved from Wikipedia on 2011-02-23.  See:
    //     http://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
    // Copy the individual tables to a file named "tld", then:
    //     cat tld | sed 's/[.]//' | sed 's/[^a-zA-Z0-9].*//' | sed 's/^/"/' | sed 's/$/"/' | tr '\n' ','
    public static final String[] GENERIC_TOP_LEVEL_DOMAINS = {"aero", "asia", "biz", "cat", "com", "coop", "edu", "gov", "info", "int", "jobs", "mil", "mobi", "museum", "name", "net", "org", "pro", "tel", "travel", "xxx"};
    public static final String[] COUNTRYCODE_TOP_LEVEL_DOMAINS = {"ac", "ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz", "ca", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cs", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "er", "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "", "il", "im", "in", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mk", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st", "su", "sv", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf", "ws", "ye", "yt", "za", "zm", "zw"};

    private static final Pattern AGENT_NICKNAMES = Pattern.compile("[a-zA-Z0-9]+");

    private static final Set<String> TOP_LEVEL_DOMAINS;

    static {
        TOP_LEVEL_DOMAINS = new HashSet<String>();
        TOP_LEVEL_DOMAINS.addAll(Arrays.asList(GENERIC_TOP_LEVEL_DOMAINS));
        TOP_LEVEL_DOMAINS.addAll(Arrays.asList(COUNTRYCODE_TOP_LEVEL_DOMAINS));
    }

    private final Properties globalProperties;
    private final Map<String, AgentProfile> agentProfiles;

    /**
     * @param properties a set of properties from which to extract agent profiles and global properties
     */
    public Configuration(final Properties properties) {
        this.globalProperties = new Properties();

        if (null == properties) {
            throw new IllegalArgumentException();
        }

        agentProfiles = new HashMap<String, AgentProfile>();
        for (String key : properties.stringPropertyNames()) {
            int i = key.indexOf(".");
            String value = properties.getProperty(key);

            // "Weird" or simple names are global.
            if (i > 0) {
                String prefix = key.substring(0, i);

                // Names beginning with a TLD plus a period, or not beginning with an agent ID plus a period, are global.
                if (!TOP_LEVEL_DOMAINS.contains(prefix)
                        && AGENT_NICKNAMES.matcher(prefix).matches()) {
                    AgentProfile p = agentProfiles.get(prefix);
                    if (null == p) {
                        p = new AgentProfile(prefix);
                        agentProfiles.put(prefix, p);
                    }

                    String newKey = key.substring(i + 1);
                    // Names beginning with an agent ID followed by a period, but nothing else, are global.
                    if (0 < newKey.length()) {
                        p.getParameters().put(newKey, value);
                        continue;
                    }
                }
            }

            // Default to treating this property as global.
            globalProperties.put(key, value);
        }
    }

    /**
     * @param properties a file containing a set of properties from which to extract agent profiles and global properties
     * @throws java.io.IOException if properties cannot be loaded from the given file
     */
    public Configuration(final File properties) throws IOException {
        this(load(properties));
    }

    /**
     * @param properties an input stream containing a set of properties from which to extract agent profiles and global properties
     * @throws java.io.IOException if properties cannot be loaded from the given input stream
     */
    public Configuration(final InputStream properties) throws IOException {
        this(load(properties));
    }

    private static Properties load(final InputStream is) throws IOException {
        if (null == is) {
            throw new IllegalArgumentException();
        }

        Properties p = new Properties();
        p.load(is);
        return p;
    }

    private static Properties load(final File file) throws IOException {
        if (null == file) {
            throw new IllegalArgumentException();
        }

        Properties p = new Properties();

        InputStream is = new FileInputStream(file);
        try {
            p.load(is);
        } finally {
            is.close();
        }

        return p;
    }

    /**
     * @return all global properties contained in this configuration
     */
    public Properties getGlobalProperties() {
        return globalProperties;
    }

    /**
     * @return all agent profiles contained in this configuration
     */
    public Map<String, AgentProfile> getAgentProfiles() {
        return agentProfiles;
    }

    /**
     * A set of key/value pairs describing an agent with a given nickname.
     */
    public class AgentProfile {
        private final String id;
        private final TypedProperties parameters;

        private AgentProfile(final String id) {
            this.id = id;
            this.parameters = new TypedProperties();
        }

        /**
         * @return the unique nickname of the agent.
         *         This id is used only for configuration and logging purposes, and is not visible agents on other platforms.
         */
        public String getNickname() {
            return id;
        }

        /**
         * @return the agent's configuration parameters
         */
        public TypedProperties getParameters() {
            return parameters;
        }
    }
}
