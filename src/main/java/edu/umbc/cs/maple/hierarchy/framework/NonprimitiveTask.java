package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.config.solver.SolverConfig;

public class NonprimitiveTask extends Task {

    protected SolverConfig solverConfig;
    protected GoalFailTF tf;
    protected GoalFailRF rf;

    public NonprimitiveTask(Task[] children, ActionType actionType, OOSADomain domain, StateMapping stateMapper, GoalFailTF tf, GoalFailRF rf, SolverConfig solverConfig) {
        super(children, actionType, domain, stateMapper);
        this.tf = tf; //new GoalFailTF(compl, null, fail, null);
        this.rf = rf; //new GoalFailRF(this.tf, defaultReward, noopReward);
        this.solverConfig = solverConfig;
    }

    public NonprimitiveTask(OOSADomain baseDomain) {
        super();
        // should only be used for wrapping the baseDomain, for non-hierarchical methods like Q Learning
        this.domain = baseDomain;
    }



    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * uses the defined rewardTotal function to assign rewardTotal to states
     * @param s the original state that is being transitioned from
     * @param a the action associated with the grounded version of this task
     * @param sPrime the next state that is being transitioned into
     * @param params the parameters of the grounded version of this task
     * @return the rewardTotal assigned to s by the rewardTotal function
     */
    @Override
    public double reward(State s, Action a, State sPrime, String[] params){
        return rf.reward(s, a, sPrime, params);
    }

    /**
     * customise the rewardTotal function
     * @param rf the rewardTotal function which should take in a state and
     * grounded action
     */
    public void setRF(GoalFailRF rf){
        this.rf = rf;
    }

    public SolverConfig getSolverConfig() {
        return solverConfig;
    }

    public void setSolverConfig(SolverConfig solverConfig) {
        this.solverConfig = solverConfig;
    }

    public GoalFailTF getTf() {
        return tf;
    }

    public void setTf(GoalFailTF tf) {
        this.tf = tf;
    }

    public GoalFailRF getRf() {
        return rf;
    }

    public void setRf(GoalFailRF rf) {
        this.rf = rf;
    }

    @Override
    public boolean isFailure(State s, String[] params, boolean unsetParams) {
        boolean atFailure = tf.atFailure(s, params);
        tf.setGoalParams(null);
        tf.setFailParams(null);
        return atFailure;
    }

    @Override
    public boolean isComplete(State s, String[] params, boolean unsetParams){
        boolean atGoal = tf.atGoal(s, params);
        tf.setGoalParams(null);
        tf.setFailParams(null);
        return atGoal;
    }

}
