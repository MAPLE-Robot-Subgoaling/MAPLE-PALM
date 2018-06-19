package edu.umbc.cs.maple.palm.agent;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.TransitionProb;
import edu.umbc.cs.maple.utilities.DiscountProvider;

import java.util.List;

public abstract class PALMModel extends FactoredModel {

    @Override
    public EnvironmentOutcome sample(State s, Action a) {
        List<TransitionProb> tps = transitions(s, a);
        double sample = RandomFactory.getMapped(0).nextDouble();
        double sum = 0;
        for(TransitionProb tp : tps){
            sum += tp.p;
            if(sample < sum){
                return tp.eo;
            }
        }

        throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
    }

    @Override
    public abstract boolean terminal(State s);

    @Override
    public abstract List<TransitionProb> transitions(State s, Action a);

    public abstract void updateModel(EnvironmentOutcome result, int stepsTaken);

    public abstract DiscountProvider getDiscountProvider();

    public abstract boolean isConvergedFor(State s, Action a, State sPrime);

}
