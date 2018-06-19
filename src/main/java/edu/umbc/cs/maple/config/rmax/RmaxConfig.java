package edu.umbc.cs.maple.config.rmax;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;

import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_DOUBLE;
import static edu.umbc.cs.maple.config.ExperimentConfig.UNSET_INT;

public class RmaxConfig {
    public double vmax = UNSET_DOUBLE;
    public int threshold = UNSET_INT;
    public double max_delta = UNSET_DOUBLE;
    public double max_delta_rmaxq = UNSET_DOUBLE;
    public int max_iterations_in_model = UNSET_INT;
    public Boolean use_multitime_model = null;
    public Boolean wait_for_children = null;

    public boolean validate() {
        if (vmax == UNSET_DOUBLE) { return false; }
        if (threshold == UNSET_INT) { return false; }
        if (max_delta == UNSET_DOUBLE) { return false; }
        if (max_delta_rmaxq == UNSET_DOUBLE) { return false; }
        if (max_iterations_in_model== UNSET_INT) { return false; }
        if (use_multitime_model == null) { return false; }
        if (wait_for_children == null) { return false; }
        return true;
    }

}
