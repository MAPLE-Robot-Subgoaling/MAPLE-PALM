package taxi.rmaxq;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import rmaxq.framework.GroundedTask;
import rmaxq.framework.NonPrimitiveTaskNode;
import rmaxq.framework.TaskNode;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

/**
 * Created by ngopalan on 8/14/16.
 */
public class PutTaskNode extends NonPrimitiveTaskNode{

//    OOSADomain l1Domain;

	public static String ACTION_PUT = "put";
    ActionType putType;
    protected String[] passenders, locations;
    protected List<GroundedTask> gts;
    public PutTaskNode(String[] passes, String[] locs, TaskNode[] children){
        this.name = ACTION_PUT;
        this.taskNodes = children;
        this.passenders = passes;
        this.locations = locs;
        
        gts = new ArrayList<GroundedTask>();
        for(String pass : passenders){
        	for(String loc : locations){
        		gts.add(new GroundedTask(this, new SimpleAction(ACTION_PUT + "_" + pass +"_" + loc)));
        	}
        }
    }

    @Override
    public Object parametersSet(State s) {
        List<String[]> params = new ArrayList<String[]>();
        for(String loc: locations){
        	for(String pass: passenders){
        		params.add(new String[]{loc,pass});
        		}
        }
        return null;
    }

    @Override
    public boolean terminal(State s, Action action) {
        String[] act = action.actionName().split("_");
        String passName = act[1], goalName = act[2];
        TaxiState st = (TaxiState)s;
        for(TaxiPassenger p : st.passengers){
        	for(TaxiLocation l : st.locations){
        		if(p.name().equals(passName)){
        			if(!p.inTaxi)
        				return true;
        			if(l.name().equals(goalName)){
	        			if(p.x == l.x && p.y == l.y){
	        				return true;
	        			}
        			}
        		}
        	}
        }
        return false;
    } 


    @Override
    public List<GroundedTask> getApplicableGroundedTasks(State s) {
        List<GroundedTask> gtList = new ArrayList<GroundedTask>();
        TaxiState st = (TaxiState)s;
        for(GroundedTask gt: gts){
        	String pass = gt.actionName().split("_")[1];
        	for(TaxiPassenger p : st.passengers){
        		if(p.name().equals(pass)){
        			if(p.inTaxi ){
                		gtList.add(gt);
                	}		
        		}
        	}
        }
        return gtList;
    }
    
}
