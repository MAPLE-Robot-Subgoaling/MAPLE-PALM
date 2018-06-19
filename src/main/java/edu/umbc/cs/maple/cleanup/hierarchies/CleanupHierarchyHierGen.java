package edu.umbc.cs.maple.cleanup.hierarchies;

import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.config.cleanup.CleanupConfig;
import edu.umbc.cs.maple.hierarchy.framework.Task;

public class CleanupHierarchyHierGen extends CleanupHierarchy {

    @Override
    public Task createHierarchy(ExperimentConfig config, boolean plan) {
        throw new RuntimeException("Error: cleanup hiergen not implemented");
    }
}
