package edu.umbc.cs.maple.amdp.planning;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import edu.umbc.cs.maple.hierarchy.framework.*;
import edu.umbc.cs.maple.utilities.BoundedRTDP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AMDPPlanner {

    private Task root;

    private Map<String, Policy> taskPolicies;

    private double gamma;

    private HashableStateFactory hsf;

    private double maxDelta;

    private int maxSteps;

    private int maxRollouts;

    private Map<String, GroundedTask> actionMap;

    public AMDPPlanner(Task root, double gamma, HashableStateFactory hsf, double maxDelta, int maxRollouts, int maxSteps) {
        this.root = root;
        this.gamma = gamma;
        this.hsf = hsf;
        this.maxDelta = maxDelta;
        this.maxRollouts = maxRollouts;
        this.maxSteps = maxSteps;
        this.actionMap = new HashMap<>();
        this.taskPolicies = new HashMap<>();
    }

    public void resetSolver() {
        this.actionMap.clear();
        this.taskPolicies.clear();
    }

    public Episode planFromState(SADomain baseDomain, State initialBaseState){
        State rootState = root.mapState(initialBaseState);
        GroundedTask solve = root.getAllGroundedTasks(rootState).get(0);
        Episode e = new Episode(initialBaseState);
        SimulatedEnvironment env = new SimulatedEnvironment(baseDomain, initialBaseState);
        return solveTask(env, e, null, solve);
    }

    public Episode solveTask(Environment env, Episode e, GroundedTask parent, GroundedTask task){
        if(task.isPrimitive()){
            Action a = task.getAction();
            Action unMaskedAction = a;
            //somewhat generalized unmasking:
            //copy the action, and unmask the copy, execute the unmasked action
            //this allows the task to always store the masked version for model/planning purposes
            if (parent.isMasked()) {
                unMaskedAction = a.copy();
                //for now, reliant on parent of masked task to be unmasked. This may not be a safe assumption
                //there may be a need to traverse arbitrarily far up the task hierarchy to find an unmasked ancestor
                //in order to recover the true parameters.
                String trueParameters = ((ObjectParameterizedAction)parent.getAction()).getObjectParameters()[0];
                ((ObjectParameterizedAction) unMaskedAction).getObjectParameters()[0] = trueParameters;
            }
            EnvironmentOutcome result = env.executeAction(unMaskedAction);
            e.transition(result);
        } else {

            State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
            State currentState = task.mapState(baseState);

            //get the policy for the current task and start state and execute
            //it till task is completed or it fails
            Policy taskPolicy = getPolicy(task, currentState);
            while(
                    !(task.isFailure(currentState) || task.isComplete(currentState))
                            && e.actionSequence.size() < maxSteps
                    ) {
                Action a = taskPolicy.action(currentState);
                GroundedTask child = getChildTask(task, a, currentState);
                System.out.println(child);
                //recurse to solve the chosen subtask
                solveTask(env, e, task, child);

                //project the current base state into the current task's state space
                baseState = e.stateSequence.get(e.stateSequence.size() - 1);
                currentState = task.mapState(baseState);
            }
        }
        return e;
    }

    private Policy getPolicy(GroundedTask t, State s){

        String taskName = t.toString();
        //try to get the policy for task and state
        Policy p = this.taskPolicies.get(taskName);
        if(p == null){

            OOSADomain domain = t.getDomain();
            String[] params = NonprimitiveTask.parseParams(t.getAction());
            bindTaskParametersToDomainModel(domain, params);

            //plan over the modified domain to solve the task
            BoundedRTDP brtdp = new BoundedRTDP(
                    domain,
                    gamma,
                    hsf,
                    new ConstantValueFunction(0),
                    new ConstantValueFunction(1),
                    maxDelta,
                    maxRollouts
            );
            p = brtdp.planFromState(s);
            Episode debugEpisode = PolicyUtils.rollout(p, s, domain.getModel());
            System.out.println(debugEpisode.actionSequence);
        }
        return p;
    }

    private void bindTaskParametersToDomainModel(OOSADomain domain, String[] params) {
        FactoredModel model = ((FactoredModel)domain.getModel());
        GoalFailTF tf = (GoalFailTF) model.getTf();
        tf.setGoalParams(params);
        tf.setFailParams(params);
        GoalFailRF rf = (GoalFailRF) model.getRf();
        rf.setTf(tf);
    }

    private GroundedTask getChildTask(GroundedTask t, Action a, State s){
        String childTaskName = a.toString();
        GroundedTask childTask = this.actionMap.get(childTaskName);
        if(childTask == null){
            List<GroundedTask> children = t.getGroundedChildTasks(s);
            for(GroundedTask child : children){
                this.actionMap.put(child.toString(), child);
            }
            childTask = this.actionMap.get(a.toString());
        }
        return childTask;
    }

}
