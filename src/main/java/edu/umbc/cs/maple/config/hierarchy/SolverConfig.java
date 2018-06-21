package edu.umbc.cs.maple.config.hierarchy;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.singleagent.SADomain;

public abstract class SolverConfig {

    public abstract MDPSolver generateSolver();

    public abstract void setDomain(SADomain domain);
}
