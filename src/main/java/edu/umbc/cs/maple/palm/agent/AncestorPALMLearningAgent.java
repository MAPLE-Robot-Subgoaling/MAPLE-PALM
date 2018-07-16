package edu.umbc.cs.maple.palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.rmax.agent.UpdateBasedRmaxModel;
import edu.umbc.cs.maple.palm.rmax.agent.UpdateBasedRmaxModel.TimedEnvironmentOutcome;

import java.util.*;

public class AncestorPALMLearningAgent extends PALMLearningAgent {
    public AncestorPALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        super(root, modelGenerator, hsf, config);
    }

    @Override
    protected boolean updateModel(GroundedTask task, State state, State abstractState, Action action, State statePrime, State abstractStatePrime, int stepsTaken) {
        return this.updateAncestorModel(task, action, stepsTaken);
    }

    protected boolean updateAncestorModel(GroundedTask task, Action a, int stepsTaken){
        Map<HashableState, List<TimedEnvironmentOutcome>> updateByState = new HashMap<>();
        System.out.println("Task "+task.toString() + " took "+a.toString()+" in "+ stepsTaken + " steps, with:");
int updateCount = 0;
        int l = e.stateSequence.size();

        State baseStartState;
        State abstractStartState;
        State baseEndState = e.stateSequence.get(l-1);
        State abstractEndState = task.mapState(baseEndState);
        int pathLength;
       for(int startStateIndex = l-(1+stepsTaken); startStateIndex < l-1; startStateIndex++){
           pathLength = l-(startStateIndex+1);
           baseStartState = e.stateSequence.get(startStateIndex);
           abstractStartState = task.mapState(baseStartState);
           double taskReward = task.getReward(abstractStartState, a, abstractEndState, getParams(task));
           updateCount++;
           TimedEnvironmentOutcome teo = new TimedEnvironmentOutcome(abstractStartState, a, abstractEndState,taskReward, pathLength, false);
           HashableState hs = this.hashingFactory.hashState(abstractStartState);
           updateByState.putIfAbsent(hs,new ArrayList<>());
           updateByState.get(hs).add(teo);
       }
       System.out.println(updateCount +" updates!");
       PALMModel model = getModel(task);
       return ((UpdateBasedRmaxModel) model).batchUpdateModel(updateByState, getParams(task));
    }

}
