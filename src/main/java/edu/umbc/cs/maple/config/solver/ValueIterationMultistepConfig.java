package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.singleagent.SADomain;

public class ValueIterationMultistepConfig extends SolverConfig{
    @Override
    public MDPSolver generateSolver() {
        return null;
    }

    @Override
    public void setDomain(SADomain domain) {

    }
}
