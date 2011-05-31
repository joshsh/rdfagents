package net.fortytwo.rdfagents.jade;

import jade.wrapper.AgentController;
import net.fortytwo.rdfagents.RDFAgents;
import net.fortytwo.rdfagents.data.DatasetFactory;
import net.fortytwo.rdfagents.data.DatasetQuery;
import net.fortytwo.rdfagents.data.RDFContentLanguage;
import net.fortytwo.rdfagents.messaging.query.QueryClient;
import net.fortytwo.rdfagents.model.AgentReference;
import net.fortytwo.rdfagents.model.Dataset;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Play {

    public static void main(String args[]) {
        try {


        } catch (Throwable t) {
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

}