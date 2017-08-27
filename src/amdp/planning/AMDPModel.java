package amdp.planning;

import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import hierarchy.framework.GroundedTask;

public class AMDPModel implements FullModel {

	/**
	 * the full designed model perhaps for larger actionset 
	 */
	private FullModel baseModel;
	
	/**
	 * the task that this model is adapting the base model for
	 */
	private GroundedTask task;
	
	/**
	 * create a specific model for the given task 
	 * using the dynamics in the given model
	 * @param t the task this model is representing
	 * @param mod the full model that the task is a part of
	 */
	public AMDPModel(GroundedTask t, FullModel mod) {
		this.task = t;
		this.baseModel = mod;
	}
	
	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		List<TransitionProb> tps = transitions(s, a);
		double sample = RandomFactory.getMapped(0).nextDouble();
		double sum = 0;
		for(TransitionProb tp : tps){
			sum += tp.p;
			if(sample < sum){
				return tp.eo;
			}
		}
		
		throw new RuntimeException("Probabilities don't sum to 1.0: " + sum);
	}

	/**
	 * The terminal function is specific to the task
	 */
	@Override
	public boolean terminal(State s) {
		return task.isComplete(s) || task.isFailure(s);
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
 		List<TransitionProb> tps = baseModel.transitions(s, a);
 		
 		//adjust the reward and terminal values to be relevant for the task
		for(TransitionProb tp : tps){
			tp.eo.r = task.getReward(tp.eo.op);
			tp.eo.terminated = terminal(tp.eo.op);
		}
		return tps;
	}

}
