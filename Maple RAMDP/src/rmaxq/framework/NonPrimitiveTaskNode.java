package rmaxq.framework;


import burlap.behavior.policy.Policy;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;

/**
 * This is a task node from the MAXQ paper. Such a task node is associated with a grounded action,
 * and has a MAXQNodes as children.
 * Created by ngopalan on 5/6/16.
 */
public abstract class NonPrimitiveTaskNode implements TaskNode{

    protected TaskNode[] taskNodes;
    protected Policy policy;
    protected String name;
    protected OOSADomain domain;
    protected TerminalFunction tf;
    protected RewardFunction rf;
    
    public OOSADomain getDomain(){
    	return domain;
    }
    
    public boolean isTaskPrimitive(){
        return false;
    }

    @Override
    public String name(){
        return name;
    }

    public TaskNode[] getChildren(){
        return taskNodes; 
    }

    public void setTaskNodes(TaskNode[] taskNodes) {
        this.taskNodes = taskNodes;
    }


    public abstract Object parametersSet(State s);

    public boolean hasHashingFactory(){
        return false;
    }

    public HashableState hashedState(State s, GroundedTask childTask){
        System.err.println("Tried to get hashable state when not set at the node!");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonPrimitiveTaskNode that = (NonPrimitiveTaskNode) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    // this is the reward function for the grounded task
    
    public double pseudoRewardFunction(State s, Action a){
//        if(terminal(s, a))
//        	return 100;
        return 0;
    }


}
