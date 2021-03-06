package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class GoalFailRF implements RewardFunction {


    public static final double PSEUDOREWARD_ON_GOAL = 10.0;
    public static final double PSEUDOREWARD_ON_FAIL = -10.0;

    protected double rewardGoal = PSEUDOREWARD_ON_GOAL;
    protected double rewardFail = PSEUDOREWARD_ON_FAIL;
    protected double rewardDefault = 0.0;
    protected double rewardNoop = 0.0;

    protected GoalFailTF tf;

    public GoalFailRF() {
        // for de/serialization
    }

    public GoalFailRF(GoalFailTF tf) {
        this.tf = tf;
    }

    public GoalFailRF(GoalFailTF tf, double rewardDefault, double rewardNoop) {
        this.tf = tf;
        this.rewardDefault = rewardDefault;
        this.rewardNoop = rewardNoop;
    }

    public GoalFailRF(GoalFailTF tf, double rewardGoal, double rewardFail, double rewardDefault, double rewardNoop) {
        this.tf = tf;
        this.rewardGoal = rewardGoal;
        this.rewardFail = rewardFail;
        this.rewardDefault = rewardDefault;
        this.rewardNoop = rewardNoop;
    }

    public double reward(State state, Action action, State sPrime, String[] params) {
        tf.setGoalParams(params);
        tf.setFailParams(params);
        double r = reward(state, action, sPrime);
        tf.setGoalParams(null);
        tf.setFailParams(null);
        return r;
    }

    @Override
    public double reward(State state, Action action, State sPrime) {
        double r = rewardDefault;
        if (tf.atGoal(sPrime)) {
            r += rewardGoal;
        } else if (tf.atFailure(sPrime)) {
            r += rewardFail;
        } else if (sPrime.equals(state)) {
            r += rewardNoop;
        } else {
            // neither goal nor failure
        }
        return r;
    }

    public double getRewardGoal() {
        return rewardGoal;
    }

    public void setRewardGoal(double rewardGoal) {
        this.rewardGoal = rewardGoal;
    }

    public double getRewardFail() {
        return rewardFail;
    }

    public void setRewardFail(double rewardFail) {
        this.rewardFail = rewardFail;
    }

    public double getRewardDefault() {
        return rewardDefault;
    }

    public void setRewardDefault(double rewardDefault) {
        this.rewardDefault = rewardDefault;
    }

    public double getRewardNoop() {
        return rewardNoop;
    }

    public void setRewardNoop(double rewardNoop) {
        this.rewardNoop = rewardNoop;
    }

    public GoalFailTF getTf() {
        return tf;
    }

    public void setTf(GoalFailTF tf) {
        this.tf = tf;
    }

}
