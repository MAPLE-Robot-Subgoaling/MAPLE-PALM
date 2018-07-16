package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

public class ValueIterationMultistepConfig extends SolverConfig{
    public ValueIterationMultistepConfig (){
        this.setType("ValueIterationMultiStep");
    }
    @Override
    public Planner generateSolver(ValueFunction knownValueFunction) {

        ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterations, discountProvider);
        planner.toggleReachabiltiyTerminalStatePruning(true);
//		planner.toggleReachabiltiyTerminalStatePruning(false);
        if (knownValueFunction != null) {
            planner.setValueFunctionInitialization(knownValueFunction);
        }

        return planner;
    }
}
