package edu.umbc.cs.maple.palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.TransitionProb;
import edu.umbc.cs.maple.utilities.ConstantDiscountProvider;
import edu.umbc.cs.maple.utilities.DiscountProvider;

import java.util.List;

public class ExpertPALMModel extends PALMModel {

    private FactoredModel baseModel;
    protected DiscountProvider discountProvider;

    public ExpertPALMModel(FactoredModel baseModel, double gamma){
        this.baseModel = baseModel;
        this.discountProvider = new ConstantDiscountProvider(gamma);
    }

    @Override
    public boolean terminal(State s) {
        return baseModel.terminal(s);
    }

    @Override
    public List<TransitionProb> transitions(State s, Action a) {
        return baseModel.transitions(s, a);
    }

    @Override
    public boolean updateModel(EnvironmentOutcome result, int stepsTaken, String[] params) { return true; }

    @Override
    public DiscountProvider getDiscountProvider() {
        return discountProvider;
    }

    @Override
    public boolean isConvergedFor(State s, Action a, State sPrime) {
        return true;
    }

}
