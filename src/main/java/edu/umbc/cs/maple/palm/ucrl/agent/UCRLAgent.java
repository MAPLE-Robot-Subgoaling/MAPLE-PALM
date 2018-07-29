package edu.umbc.cs.maple.palm.ucrl.agent;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.utilities.DiscountProvider;
import edu.umbc.cs.maple.utilities.ValueIterationMultiStep;

import java.util.List;
import java.util.Set;

public class UCRLAgent implements LearningAgent {

    protected UCRLModel currentModel;
    protected HashableStateFactory hashingFactory;

    public UCRLAgent(){

        currentModel = new UCRLModel(base.terminalFunction(), );
    }

    protected Set<HashableState> getStateSet(OOSADomain domain, State start){
        return StateReachability.getReachableHashedStates(start, domain, hashingFactory);
    }

    @Override
    public Episode runLearningEpisode(Environment environment) {
        return runLearningEpisode(environment, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        Episode e = new Episode(env.currentObservation());
        State currentState = env.currentObservation();
        int steps = 0;
        while (! env.isInTerminalState() || (maxSteps != -1 && steps < maxSteps)){
            Action a = nextAction(currentState);
            EnvironmentOutcome eo = env.executeAction(a);
            e.transition(eo);
            currentModel.updateModel(eo, 1);
        }
    }

    public Action nextAction(State s){
        OOSADomain domain = task.getDomain(model);
//		double discount = model.gamma();
        DiscountProvider discountProvider = model.getDiscountProvider();
        ValueIterationMultiStep planner = new ValueIterationMultiStep(domain, hashingFactory, maxDelta, maxIterationsInModelPlanner, discountProvider);
        planner.toggleReachabiltiyTerminalStatePruning(true);
//		planner.toggleReachabiltiyTerminalStatePruning(false);
        ValueFunction knownValueFunction = task.valueFunction;
        if (knownValueFunction != null) {
            planner.setValueFunctionInitialization(knownValueFunction);
        }
        Policy policy = planner.planFromState(s);
        Action action = policy.action(s);
    }
}
