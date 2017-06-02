package taxi.rmaxq;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import rmaxq.framework.GroundedTask;
import rmaxq.framework.NonPrimitiveTaskNode;
import rmaxq.framework.TaskNode;
import taxi.TaxiRewardFunction;
import taxi.TaxiTerminationFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 8/14/16.
 */
public class RootTaskNode extends NonPrimitiveTaskNode {

    List<String[]> params = new ArrayList<String[]>();
    List<GroundedTask> groundedTasks = new ArrayList<GroundedTask>();
    

    public RootTaskNode(String name, OOSADomain domainIn, TaskNode[] children, int numPas) {
        this.name = name;
        this.params.add(new String[]{"1"});
        this.taskNodes = children;
        this.domain = domainIn;

        for(String[] param:params){
            groundedTasks.add(new GroundedTask(this, new SimpleAction(name+":"+param)));
        }
    }


    @Override
    public Object parametersSet(State s) {
        return params;
    }

    @Override
    public boolean terminal(State s, Action action) {
    	tf = new TaxiTerminationFunction();
        return this.tf.isTerminal(s);
    }

    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        return groundedTasks;
    }
}