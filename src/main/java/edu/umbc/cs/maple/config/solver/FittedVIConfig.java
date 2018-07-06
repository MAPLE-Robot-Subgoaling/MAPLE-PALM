package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;

public class FittedVIConfig  extends SolverConfig{
    public FittedVIConfig (){
        this.setType("FittedVI");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {
        return null;
    }
}
