package edu.umbc.cs.maple.palm.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossPALMLearningAgent extends PALMLearningAgent {

    public CrossPALMLearningAgent(Task root, PALMModelGenerator modelGenerator, HashableStateFactory hsf, ExperimentConfig config) {
        super(root, modelGenerator, hsf, config);
    }

    protected void updateAllOtherPossibleModels(GroundedTask parent, GroundedTask actualTask, State groundState, Action action, State groundStatePrime, int stepsTaken) {

        String actualActionName = action.actionName();
        String[] actionParams = getParams(action);
        //if the action was parameterized to some masked object, don't pollute other tasks with false transitions
        if(actualTask.isMasked() && action instanceof ObjectParameterizedAction){
            //check the intersection between action params and masked params to verify none of the params are aliases
            Set<String> maskedParams = new HashSet<>(Arrays.asList(actualTask.getMaskedParameters()));
            maskedParams.retainAll(Arrays.asList(actionParams));
            //if a single masked param was present, do not continue updating other models with the action
            //as it won't generalize (get p1: pickup alias --> pickup passenger1,
            //                        get p2: pickup alias --> pickup passenger2),
            // so {s_0, pickup alias, s_1} will not generalize to get p2 from get p1
            if(!maskedParams.isEmpty()) return;
        }

        for (String taskName : taskNames.keySet()) {

            GroundedTask task = taskNames.get(taskName);
            if (task.isPrimitive() || task == actualTask) { continue; }
            PALMModel model = getModel(task);
            String[] params = getParams(task);
            State abstractState = task.mapState(groundState);
            State abstractStatePrime = task.mapState(groundStatePrime);




            List<GroundedTask> children = task.getGroundedChildTasks(abstractState);
            for (GroundedTask childTask : children) {
                String childActionName = childTask.getAction().actionName();
                if (childActionName.equals(actualActionName)) {
                    String[] childParams = getParams(childTask);
                    if (Arrays.equals(childParams, actionParams)) {
                        double taskReward = getTaskReward(parent, task, groundState, abstractState, action, groundStatePrime, abstractStatePrime, params);//groundedTask.getReward(pastStateAbstract, action, currentStateAbstract, params);
                        EnvironmentOutcome result = new EnvironmentOutcome(abstractState, action, abstractStatePrime, taskReward, false);
                        model.updateModel(result, stepsTaken, params);
//                        System.out.println("just updated: " + taskName);
                    }
                }
            }

        }
    }

    @Override
    protected boolean updateModel(GroundedTask parent, GroundedTask actualTask, State state, State abstractState, Action action, State statePrime, State abstractStatePrime, int stepsTaken) {

//        System.out.println("\n*****\nUpdating models:\n*****\n");

        String[] actualParams = getParams(actualTask);
        double actualTaskReward = getTaskReward(parent, actualTask, state, abstractState, action, statePrime, abstractStatePrime, actualParams);
        PALMModel actualModel = getModel(actualTask);
        EnvironmentOutcome actualResult = new EnvironmentOutcome(abstractState, action, abstractStatePrime, actualTaskReward, false);
        boolean atOrBeyondThreshold = actualModel.updateModel(actualResult, stepsTaken, actualParams);

//        System.out.println("just updated: " + actualTask + "***");

        updateAllOtherPossibleModels(parent, actualTask, state, action, statePrime, stepsTaken);

        return atOrBeyondThreshold;
    }


}
