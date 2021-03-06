package edu.umbc.cs.maple.cleanup;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;

public class CleanupRF extends GoalBasedRF {

    private double noopReward;
    private double pullReward;

    public CleanupRF() {
        super((StateConditionTest) null);
        // for de/serialization
    }

    public CleanupRF(CleanupGoal goal, double rewardGoal, double rewardDefault, double rewardNoop, double rewardPull) {
        super(goal, rewardGoal, rewardDefault);
        this.noopReward = rewardNoop;
        this.pullReward = rewardPull;
    }

    public StateConditionTest getGoal() {
        return gc;
    }

    public void setGoal(StateConditionTest gc) {
        this.gc = gc;
    }

    public double getPullReward() {
        return pullReward;
    }

    public void setPullReward(double pullReward) {
        this.pullReward = pullReward;
    }

    public double getNoopReward() {
        return noopReward;
    }

    public void setNoopReward(double noopReward) {
        this.noopReward = noopReward;
    }

    @Override
    public double reward(State s, Action a, State sprime) {
        double superR = super.reward(s, a, sprime);
        double r = superR;
        if (a.actionName().equals(Cleanup.ACTION_PULL)) {
            r += pullReward;
        }
        if (s.equals(sprime)) {
            r += noopReward;
        }
        return r;
    }
}
