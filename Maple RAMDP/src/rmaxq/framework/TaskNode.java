package rmaxq.framework;



import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.List;

/**
 * These are the V nodes from the Dietterich MaxQ paper
 * Created by ngopalan on 5/5/16.
 */
public interface TaskNode {    
    boolean isTaskPrimitive();
    boolean terminal(State s, Action a);

    // here each grounded task comes with a fake action that we create
    public List<GroundedTask> getApplicableGroundedTasks(State s);

    public TaskNode[] getChildren();
    
    public String name();

    public OOSADomain getDomain();
}
