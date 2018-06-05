package cleanup.hierarchies;

import config.ExperimentConfig;
import config.cleanup.CleanupConfig;
import hierarchy.framework.Task;

public class CleanupHierarchyHiergen extends CleanupHierarchy {

    @Override
    public Task createHierarchy(ExperimentConfig config, boolean plan) {
        throw new RuntimeException("Error: cleanup hiergen not implemented");
    }
}
