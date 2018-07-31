package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.utilities.ContradictionPF;

public class GoalFailTF implements TerminalFunction {

    protected PropositionalFunction goalPF;
    protected PropositionalFunction failPF;
    protected String[] goalParams;
    protected String[] failParams;

    public GoalFailTF(){
        // for de/serialization
    }

    public GoalFailTF(DomainGoal goal) {
        this.goalPF = goal;
        this.goalParams = new String[]{}; // empty, goal has its own params embedded inside it
        this.failPF = new ContradictionPF();
        this.failParams = new String[]{};
    }

    public GoalFailTF(PropositionalFunction goalPF, String[] goalParams, PropositionalFunction failPF, String[] failParams) {
        this.goalPF = goalPF;
        this.goalParams = goalParams;
        this.failPF = failPF;
        this.failParams = failParams;
    }

    public boolean atGoal(State state) {
        OOState ooState = (OOState) state;
        return goalPF.isTrue(ooState, goalParams);
    }

    public boolean atFailure(State state) {
        OOState ooState = (OOState) state;
        return failPF.isTrue(ooState, failParams);
    }

    public boolean atGoal(State state, String[] goalParams) {
        setGoalParams(goalParams);
        boolean goal = atGoal(state);
        return goal;
    }

    public boolean atFailure(State state, String[] failParams) {
        setFailParams(failParams);
        boolean failure = atFailure(state);
        return failure;
    }

    @Override
    public boolean isTerminal(State state) {
        return atGoal(state) || atFailure(state);
    }

    public PropositionalFunction getGoalPF() {
        return goalPF;
    }

    public void setGoalPF(PropositionalFunction goalPF) {
        this.goalPF = goalPF;
    }

    public PropositionalFunction getFailPF() {
        return failPF;
    }

    public void setFailPF(PropositionalFunction failPF) {
        this.failPF = failPF;
    }

    public String[] getGoalParams() {
        return goalParams;
    }

    public void setGoalParams(String[] goalParams) {
        this.goalParams = goalParams;
    }

    public String[] getFailParams() {
        return failParams;
    }

    public void setFailParams(String[] failParams) {
        this.failParams = failParams;
    }

}
