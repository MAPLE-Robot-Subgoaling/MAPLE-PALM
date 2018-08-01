package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.config.solver.SolverConfig;

public class NonprimitiveTask extends Task {
    //tasks which are not at the base of the hierarchy

    // default rewardTotal used in nonprimitive task's pseudo-rewardTotal function
    public static double DEFAULT_REWARD = 0.000;//1;//0.000001;
    public static double NOOP_REWARD = 0.0;//-0.0001;

    protected GoalFailTF goalFailTF;
    protected GoalFailRF goalFailRF;
    public SolverConfig solver;
    /**
     * create a nunprimitive task
     * @param children the subtasks
     * @param aType the set of actions this task represents in its parent task's domain
     * @param abstractDomain the domain this task executes actions in
     * @param map the state abstraction function into the domain
     */
    public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
                            TerminalFunction tf, RewardFunction rf, SolverConfig solverConfig) {
        this(children,aType,abstractDomain,map,tf,rf);
        this.solver = solverConfig;
    }
    //used for hierarchies with abstractions
    /**
     * create a nunprimitive task
     * @param children the subtasks
     * @param aType the set of actions this task represents in its parent task's domain
     * @param abstractDomain the domain this task executes actions in
     * @param map the state abstraction function into the domain
     */
    public NonprimitiveTask(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map,
                            TerminalFunction tf, RewardFunction rf) {
        super(children, aType, abstractDomain, map);
        this.goalFailTF = (GoalFailTF) tf;
        this.goalFailRF = (GoalFailRF) rf;
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
        return goalFailRF.reward(s, a, sPrime, params);
    }

    /**
     * customise the rewardTotal function
     * @param rf the rewardTotal function which should take in a state and
     * grounded action
     */
    public void setRF(GoalFailRF rf){
        this.goalFailRF = rf;
    }

    @Override
    public boolean isFailure(State s, String[] params, boolean unsetParams) {
        boolean atFailure = goalFailTF.atFailure(s, params);
        goalFailTF.setGoalParams(null);
        goalFailTF.setFailParams(null);
        return atFailure;
    }

    @Override
    public boolean isComplete(State s, String[] params, boolean unsetParams){
        boolean atGoal = goalFailTF.atGoal(s, params);
        goalFailTF.setGoalParams(null);
        goalFailTF.setFailParams(null);
        return atGoal;
    }

    public GoalFailTF getGoalFailTF() {
        return goalFailTF;
    }

    public GoalFailRF getGoalFailRF() {
        return goalFailRF;
    }

    public SolverConfig getSolver() {
        return this.solver;
    }
}
