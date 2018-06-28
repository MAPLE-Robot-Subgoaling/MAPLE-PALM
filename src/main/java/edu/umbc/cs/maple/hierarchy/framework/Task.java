package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.MaskedParameterizedStateMapping;
import edu.umbc.cs.maple.taxi.hierarchies.interfaces.ParameterizedStateMapping;

import java.util.ArrayList;
import java.util.List;

public abstract class Task {

    /**
     * domain - the MDP representing the level of abstraction
     */
    protected OOSADomain domain;

    /**
     * actionType - the general action type acossiated with the task
     */
    private ActionType actionType;

    /**
     * children - the subtasks in a hierarchy
     */
    private Task[] children;

    /**
     * the function to map state at one level lower up to current level
     */
    private StateMapping mapper;

    private boolean masked;
    private String[] maskedParameters;
    /**
     * Setup of variables
     * @param children the task's subtasks
     * @param aType the general class of actions used by this task
     * @param abstractDomain the domain at the correct level of abstraction
     * @param map state mapper to the abstract domain
     */
    public Task(Task[] children, ActionType aType, OOSADomain abstractDomain, StateMapping map){
        this.children = children;
        this.actionType = aType;
        this.domain = abstractDomain;
        this.mapper = map;
        this.masked = (map instanceof MaskedParameterizedStateMapping);
        if(this.masked){
            maskedParameters = ((MaskedParameterizedStateMapping)this.mapper).getMaskedParameters();
        }else {
            maskedParameters = null;
        }
    }

    public Task() {
        // should only be used in serialization or for wrapping data used by non-hierarchical methods like Q Learning
    }

    /**
     * get the domain
     * @return the abstract domain for the task
     */
    public OOSADomain getDomain(){
        return domain;
    }

    public ActionType getActionType(){
        return actionType;
    }

    /**
     * Gets all parameterizations of the task availibe in s
     * @param s the current state
     * @return list of grounded tasks which gives all variations
     * of the task in the current state
     */
    public List<GroundedTask> getAllGroundedTasks(State s){
        List<Action> acts = actionType.allApplicableActions(s);
        List<GroundedTask> gts = new ArrayList<GroundedTask>();
        for(Action a : acts){
            gts.add(new GroundedTask(a, this));
        }
        return gts;
    }

    /**
     * Gets the subtasks of the current task
     * @return array of subtasks of the task
     */
    public Task[] getChildren(){
        return children;
    }

    /**
     * Projects a state at level L - 1 to level L
     * @param lowerState state at level just below current level
     * @return the same state but projected up one level
     */
    public State mapState(State lowerState, String...params){
        if(mapper instanceof ParameterizedStateMapping)
            return ((ParameterizedStateMapping)mapper).mapState(lowerState, params);
        else
            return mapper.mapState(lowerState);
    }

    /**
     * a unique ID
     * @return unique ID for the task in the hierarchy
     */
    public String getName(){
        return actionType.typeName();
    }

    /**
     *
     * @return boolean indicating whether the state mapping masks parameters
     */
    public boolean isMasked() {return this.masked;}

    /**
     * tells whether this task is in the base MDP
     * @return boolean indicating whether the task is
     * primitive (true) or composite (false)
     */
    public abstract boolean isPrimitive();

    /**
     * determines if the current task is terminated in state s which parameterization a
     * @param s the current state
     * @param params the parameters from the specific grounding of the task
     * @return boolean indicating if the action a is terminated in state s
     */
    public abstract boolean isFailure(State s, String[] params);

    public final boolean isFailure(State s, Action a) {
        String[] params = parseParams(a);
        return isFailure(s, params);
    }

    public final boolean isComplete(State s, Action a) {
        String[] params = parseParams(a);
        return isComplete(s, params);
    }

    /**
     * tests a state to determine if task is complete
     * @param s state to test
     * @param params the parameters of the specific grounded task
     * @return wether a is complete in s
     */
    public abstract boolean isComplete(State s, String[] params);

    public abstract double reward(State s, Action a, State sPrime);

    public static String[] parseParams(Action action) {
        String[] params = null;
        if (action instanceof ObjectParameterizedAction) {
            params = ((ObjectParameterizedAction) action).getObjectParameters();
        } else {
            params = new String[]{StringFormat.parameterizedActionName(action)};
        }
        return params;
    }

}
