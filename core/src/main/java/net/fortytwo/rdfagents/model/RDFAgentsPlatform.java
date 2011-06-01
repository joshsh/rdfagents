package net.fortytwo.rdfagents.model;

import net.fortytwo.rdfagents.data.DatasetFactory;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 1:59 AM
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
}
