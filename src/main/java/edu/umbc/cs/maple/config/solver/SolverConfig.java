package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.singleagent.SADomain;

public abstract class SolverConfig {
    String type;

    public abstract MDPSolver generateSolver();

    public abstract void setDomain(SADomain domain);
}
