package edu.umbc.cs.maple.config.rmax;

import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_DOUBLE;
import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_INT;
import static edu.umbc.cs.maple.hierarchy.framework.GoalFailRF.PSEUDOREWARD_ON_GOAL;

public class RmaxConfig {
    public double vmax = UNSET_DOUBLE;
    public int threshold = UNSET_INT;
    public double max_delta = UNSET_DOUBLE;
    public double max_delta_rmaxq = UNSET_DOUBLE;
    public int max_iterations_in_model = UNSET_INT;
    public Boolean use_multitime_model = null;
    public Boolean use_model_sharing = null;

    public boolean validate() {
        boolean invalid;
        invalid = vmax == UNSET_DOUBLE;//) { return false; }
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = vmax < PSEUDOREWARD_ON_GOAL;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = threshold == UNSET_INT;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = max_delta == UNSET_DOUBLE;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = max_delta_rmaxq == UNSET_DOUBLE;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = max_iterations_in_model == UNSET_INT;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = use_multitime_model == null;
        if (invalid) { throw new RuntimeException("invalid"); }
        invalid = use_model_sharing == null;
        if (invalid) { throw new RuntimeException("invalid"); }
        return !invalid;
    }

}
