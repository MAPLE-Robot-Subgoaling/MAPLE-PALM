package hierarchies.structureLearning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.CORBA.TRANSACTION_MODE;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;

public class CATrajectory {

	private List<String> actions;
	private List<RelevantEdge> edges;
	private Set<String>[] relevantVariables;
//	private Episode baseTrajectory;
	
	public CATrajectory() {
		this.actions = new ArrayList<String>();
		this.edges = new ArrayList<RelevantEdge>();
	}
	
	//parent structure  action -> variable/ R(reward) -> relevant var
	public void annotateTrajectory(Episode e, Map<String, Map<String, List<String>>> parents, FullModel model){
		actions.add("START");
		for(Action a : e.actionSequence){
			actions.add(a.actionName());
		}
		actions.add("END");
		
		relevantVariables = new Set[actions.size()];
		
		for(int i = 0; i < actions.size(); i++){
			String action = actions.get(i);
			relevantVariables[i] = new HashSet<String>();
			
			if(action.equals("START") || action.equals("END")){
				State s = e.stateSequence.get(0);
				for(Object var : s.variableKeys()){
					relevantVariables[i].add(var.toString());
				}
				continue;
			}
			
			State s = e.stateSequence.get(i - 1);
			Action a = e.actionSequence.get(i - 1);
			for(Object var : s.variableKeys()){
				boolean contextChanged = false;
				Object value = s.get(var);
				
				List<TransitionProb> tps = model.transitions(s, a);
				for(TransitionProb tp : tps){
					State sPrime = tp.eo.op;
					Object valuePrime = sPrime.get(var);
					if(!value.equals(valuePrime)){
						contextChanged = true;
					}
				}
				
				List<String> rewardVars = parents.get(action).get("R");
				if(rewardVars.contains(var.toString())){
					relevantVariables[i].add(var.toString());
				}
				
				if(contextChanged){
					List<String> parentsV = parents.get(action).get(var.toString());
					relevantVariables[i].add(var.toString());
					relevantVariables[i].addAll(parentsV);
				}
			}
		}
		
		for(int i = 0; i < actions.size() - 1; i++){
			for(String var : relevantVariables[i]){
				int next = i + 1;
				while(! relevantVariables[next].contains(var)){
					next++;
				}
				RelevantEdge edge = new RelevantEdge(i, next, var);
				edges.add(edge);
			}
		}
	}
	
	public int findEdge(int start, String variable){
		for(RelevantEdge edge : edges){
			if(edge.getStart() == start && edge.getRelavantVariable().equals(variable)){
				return edge.getEnd();
			}
		}
		return -1;
	}
	
	@Override
	public String toString(){
		String out = "";
		if(actions.size() == 0){
			out = "No actiond";
		}else{
			out = "Actions: ";
			for(String a : actions){
				out += a + " ";
			}
			out += "\n";
			
			for(RelevantEdge edge : edges){
				out += edge.getStart() + " " + edge.getEnd() + " " + edge.getRelavantVariable() + "\n";
			}
		}
		return out;
	}
	
}
