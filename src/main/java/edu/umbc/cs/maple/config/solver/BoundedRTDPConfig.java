package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.ValueFunction;
import edu.umbc.cs.maple.config.rmax.RmaxConfig;
import edu.umbc.cs.maple.utilities.BoundedRTDP;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

import static edu.umbc.cs.maple.hierarchy.framework.GoalFailRF.PSEUDOREWARD_ON_FAIL;
import static edu.umbc.cs.maple.hierarchy.framework.GoalFailRF.PSEUDOREWARD_ON_GOAL;

public class BoundedRTDPConfig extends SolverConfig {
    ValueFunction upper = new ConstantValueFunction(PSEUDOREWARD_ON_GOAL);
    ValueFunction lower = new ConstantValueFunction(PSEUDOREWARD_ON_FAIL);
    public BoundedRTDPConfig (){
        this.setType("BoundedRTDPConfig");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {
        double gamma = discountProvider.getGamma();
        BoundedRTDP planner = new BoundedRTDP(domain, gamma, hashingFactory, lower, upper, maxDelta, maxIterations);
//        planner.toggleReachabiltiyTerminalStatePruning(true);
//		planner.toggleReachabiltiyTerminalStatePruning(false);
        if (knownValueFunction != null) {
            planner.setValueFunctionInitialization(knownValueFunction);
        }
        return planner;
    }
}
