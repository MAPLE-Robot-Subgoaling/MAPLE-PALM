package edu.umbc.cs.maple.config.solver;

import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.ValueFunction;
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
