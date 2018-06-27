package edu.umbc.cs.maple.palm.ucb.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.palm.agent.PALMModel;
import edu.umbc.cs.maple.utilities.ConstantDiscountProvider;
import edu.umbc.cs.maple.utilities.DiscountProvider;

import java.util.List;

public class UCBModel extends PALMModel {

    protected DiscountProvider discountProvider;


    public UCBModel(GroundedTask root, double gamma){
        this.initializeDiscountProvider(gamma);
    }

    public void initializeDiscountProvider(double gamma) {
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }

    @Override
    public boolean terminal(State s) {
        return false;
    }

    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        return null;
    }

    @Override
    public void updateModel(EnvironmentOutcome result, int stepsTaken) {

    }

    @Override
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        return false;
    }
}
