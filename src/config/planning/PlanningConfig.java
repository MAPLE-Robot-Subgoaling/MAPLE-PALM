package config.planning;

import static config.ExperimentConfig.UNSET_INT;

public class PlanningConfig {
    public int rollouts;

    public boolean validate() {
        if (rollouts == UNSET_INT) { return false; }
        return true;
    }
}
