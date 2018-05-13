package palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.List;

public class ExpertPALMModel extends PALMModel {

    private FactoredModel baseModel;
    private Double gamma;

    public ExpertPALMModel(FactoredModel baseModel, double gamma){
        this.baseModel = baseModel;
        this.gamma = gamma;
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
    public void updateModel(EnvironmentOutcome result, int stepsTaken) {}

    @Override
    public double gamma() {
        if (gamma == null) {
            throw new RuntimeException("Warning: must specify a gamma / discount factor");
        } else {
            return gamma;
        }
    }
}
