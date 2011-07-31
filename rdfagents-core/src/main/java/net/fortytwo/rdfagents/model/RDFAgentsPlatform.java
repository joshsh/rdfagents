package net.fortytwo.rdfagents.model;

import net.fortytwo.rdfagents.data.DatasetFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class RDFAgentsPlatform {
    protected final DatasetFactory datasetFactory;
    protected final String name;

    public RDFAgentsPlatform(final String name,
                             final DatasetFactory datasetFactory) {
        this.name = name;
        this.datasetFactory = datasetFactory;
    }

    public String getName() {
        return name;
    }

    public DatasetFactory getDatasetFactory() {
        return datasetFactory;
    }

    public abstract void shutDown();
}
