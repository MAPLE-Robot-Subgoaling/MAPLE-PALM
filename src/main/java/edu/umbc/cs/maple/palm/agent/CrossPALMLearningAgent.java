package edu.umbc.cs.maple.palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import java.util.Arrays;
import java.util.List;

public class CrossPALMLearningAgent extends PALMLearningAgent {

    public CrossPALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        super(root, modelGenerator, hsf, config);
    }

    protected void updateAllOtherPossibleModels(GroundedTask actualTask, State state, Action action, State statePrime, int stepsTaken) {

        String actualActionName = action.actionName();
        String[] actionParams = getParams(action);
        for (String taskName : taskNames.keySet()) {

            GroundedTask task = taskNames.get(taskName);
            if (task.isPrimitive() || task == actualTask) { continue; }
            PALMModel model = getModel(task);
            String[] params = getParams(task);
            State abstractState = task.mapState(state);
            State abstractStatePrime = task.mapState(statePrime);
            List<GroundedTask> children = task.getGroundedChildTasks(abstractState);
            for (GroundedTask childTask : children) {
                String childActionName = childTask.getAction().actionName();
                if (childActionName.equals(actualActionName)) {
                    String[] childParams = getParams(childTask);
                    if (Arrays.equals(childParams, actionParams)) {
                        double taskReward = getTaskReward(task, abstractState, action, abstractStatePrime, params);//groundedTask.getReward(pastStateAbstract, action, currentStateAbstract, params);
                        EnvironmentOutcome result = new EnvironmentOutcome(abstractState, action, abstractStatePrime, taskReward, false);
                        model.updateModel(result, stepsTaken, params);
//                        System.out.println("just updated: " + taskName);
                    }
                }
            }

        }
    }

    @Override
    protected boolean updateModel(GroundedTask actualTask, State state, State abstractState, Action action, State statePrime, State abstractStatePrime, int stepsTaken) {

//        System.out.println("\n*****\nUpdating models:\n*****\n");

        String[] actualParams = getParams(actualTask);
        double actualTaskReward = getTaskReward(actualTask, abstractState, action, abstractStatePrime, actualParams);
        PALMModel actualModel = getModel(actualTask);
        EnvironmentOutcome actualResult = new EnvironmentOutcome(abstractState, action, abstractStatePrime, actualTaskReward, false);
        boolean atOrBeyondThreshold = actualModel.updateModel(actualResult, stepsTaken, actualParams);

//        System.out.println("just updated: " + actualTask + "***");

        updateAllOtherPossibleModels(actualTask, state, action, statePrime, stepsTaken);

        return atOrBeyondThreshold;
    }


}
