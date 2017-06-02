package rmaxq.framework;

import burlap.mdp.core.action.Action; 
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ngopalan on 5/6/16.
 */
public abstract class PrimitiveTaskNode implements TaskNode {
    protected ActionType actionType;
    protected OOSADomain domain;
    protected RewardFunction rf;
    

    public String name(){
    	return this.actionType.typeName();
    }

    public TaskNode[] getChildren(){
    	return null;
    }
    
    public OOSADomain getDomain(){
    	return domain;
    }
    
    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public boolean isTaskPrimitive(){
        return true;
    }

    public boolean terminal(State s, Action action) {
        return true;
    }

    public List<GroundedTask> getApplicableGroundedTasks(State s){
        List<Action> gaList = ActionUtils.allApplicableActionsForTypes(Arrays.asList((ActionType)actionType), s);
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        for(Action ga:gaList){
            gtList.add(new GroundedTask(this, ga));
        }
        return gtList;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimitiveTaskNode that = (PrimitiveTaskNode) o;

        return actionType != null ? actionType.equals(that.actionType) : that.actionType== null;
    }
}
