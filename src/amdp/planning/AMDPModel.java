package amdp.planning;

import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import hierarchy.framework.GroundedTask;

public class AMDPModel implements FullModel {

	private FullModel baseModel;
	
	private GroundedTask task;
	
	public AMDPModel(GroundedTask t, FullModel mod) {
		this.task = t;
		this.baseModel = mod;
	}
	
	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		List<TransitionProb> tps = transitions(s, a);
		double sample = Math.random();
		double sum = 0;
		for(TransitionProb tp : tps){
			sum += tp.p;
			if(sample < sum){
				return tp.eo;
			}
		}
		
		throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
	}

	@Override
	public boolean terminal(State s) {
		return task.isComplete(s) || task.isFailure(s);
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
 		List<TransitionProb> tps = baseModel.transitions(s, a);
		for(TransitionProb tp : tps){
			tp.eo.r = task.getReward(tp.eo.op);
			tp.eo.terminated = terminal(tp.eo.op);
		}
		return tps;
	}

}
