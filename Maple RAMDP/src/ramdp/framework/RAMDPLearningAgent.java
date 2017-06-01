package ramdp.framework;

import java.util.Map;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.singleagent.environment.Environment;

public class RAMDPLearningAgent implements LearningAgent{

	/**
	 * The root of the task hierarchy
	 */
	private GroundedTask root;
	
	/**
	 * r-max "m" parameter
	 */
	private int rmaxThreshold;
	
	/**
	 * collection of models for each task
	 */
	private Map<GroundedTask, RAMDPModel> models;
	
	/**
	 * Steps currently taken
	 */
	private int steps;
	
	/**
	 * 
	 * @param root
	 * @param threshold
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold) {
		this.rmaxThreshold = threshold;
		this.root = root;
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		steps = 0;
		Episode e = new Episode(env.currentObservation());
		return solveTask(root, e);
	}

	protected Episode solveTask(GroundedTask task, Episode e){
		
		return e;
	}
}
