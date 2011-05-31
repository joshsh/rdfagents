package net.fortytwo.rdfagents.jade;

import net.fortytwo.rdfagents.jade.sesame.RDFAgentsJadePlatform;
import net.fortytwo.rdfagents.util.Configuration;

import java.io.File;

/**
 * User: josh
 * Date: 2/23/11
 * Time: 7:12 PM
 */
public class Demo {
    public static void main(final String args[]) {
        try {
            if (1 == args.length) {
                Configuration config = new Configuration(new File(args[0]));
                new RDFAgentsJadePlatform(config).launch();
            } else {
                printUsage();
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:  rdfagents [configuration file]");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://wiki.github.com/joshsh/rdfagents>.");
    }
}
