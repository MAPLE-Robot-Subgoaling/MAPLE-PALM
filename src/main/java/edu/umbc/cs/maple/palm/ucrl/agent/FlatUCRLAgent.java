package edu.umbc.cs.maple.palm.ucrl.agent;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlatUCRLAgent implements LearningAgent {

    protected UCRLModel currentModel;
    protected HashableStateFactory hashingFactory;
    protected List<HashableState> reachableStates;
    protected double gamma;
    protected double maxDelta;
    protected double rmax;

    public FlatUCRLAgent(Task t, State start, double gamma, double maxDelta, double rmax,
                         HashableStateFactory hsf){
        this.gamma = gamma;
        this.maxDelta = maxDelta;
        this.rmax = rmax;

        this.hashingFactory = hsf;
        GroundedTask gt = t.getAllGroundedTasks(start).get(0);
        OOSADomain base = gt.getDomain();
        Set<HashableState> stateSet = getStateSet(base, start);
        reachableStates = new ArrayList<HashableState>(stateSet);
        currentModel = new UCRLModel(gt, reachableStates, gamma, maxDelta, rmax, hashingFactory);
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
        return e;
    }

    protected Action nextAction(State s) {
        return currentModel.nextAction(s);
    }
}
