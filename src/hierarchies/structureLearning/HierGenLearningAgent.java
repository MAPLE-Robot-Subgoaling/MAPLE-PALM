package hierarchies.structureLearning;

import action.models.CreateActionModels;
import action.models.TrajectoryGengerator;
import action.models.VariableTree;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HierGenLearningAgent implements LearningAgent {

	private RAMDPLearningAgent ramdpLearningAgent;
	private int threshold;
	private double gamma;
	private double discount;
	private double rmax;
	private HashableStateFactory HashingFactory;
	private double maxDelta;
	private OOSADomain baseDomain;
	private int numTrajectories;
	private String treeDirerctory;
	private StateGenerator trainingStates;
	private int numIteraions;

	public HierGenLearningAgent(OOSADomain baseDomain, int numTrajectories, double gamma, int threshold,
								double discount, double rmax, HashableStateFactory hs, double delta,
								String treeDirectory, StateGenerator trainingStates, int maxIterations){
		this.baseDomain = baseDomain;
		this.numTrajectories = numTrajectories;
		this.gamma = gamma;
		this.threshold = threshold;
		this.discount = discount;
		this.rmax = rmax;
		this.HashingFactory = hs;
		this.maxDelta = delta;
		this.numIteraions = maxIterations;
		this.treeDirerctory = treeDirectory;
		this.trainingStates = trainingStates;
	}

	public HierGenLearningAgent(OOSADomain baseDomain, int numTrajectories, int threshold, double discount, double rmax,
								HashableStateFactory hs, double delta, int maxIterations){
		this.baseDomain = baseDomain;
		this.numTrajectories = numTrajectories;
		this.threshold = threshold;
		this.discount = discount;
		this.rmax = rmax;
		this.HashingFactory = hs;
		this.maxDelta = delta;
		this.numIteraions = maxIterations;
		this.treeDirerctory = null;
	}

	protected Task createHierarchy(){
		Map<String, Map<String, VariableTree>> actionMoodels;

		if(treeDirerctory == null){
			List<Episode> trajectories = TrajectoryGengerator.generateQLearnedTrajectories
					(trainingStates, numTrajectories, baseDomain, gamma, HashingFactory);
			actionMoodels = CreateActionModels.createModels(trajectories);
		} else {
			actionMoodels = CreateActionModels.readTreeFiles(treeDirerctory);
		}

		List<Episode> trajectories = TrajectoryGengerator.generateVIPlannedTrajectories
				(trainingStates, numTrajectories, baseDomain, gamma, HashingFactory, maxDelta, numIteraions);
		List<CATrajectory> caTrajectories = new ArrayList<CATrajectory>();
		for(Episode trajectory : trajectories){
			CATrajectory cat = new CATrajectory();
			cat.annotateTrajectory(trajectory, actionMoodels, baseDomain.getModel());
			caTrajectories.add(cat);
		}


	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		if(ramdpLearningAgent == null){
			Task root = createHierarchy();
			GroundedTask gRoot = root.getAllGroundedTasks(env.currentObservation()).get(0);
			ramdpLearningAgent = new RAMDPLearningAgent(gRoot, threshold, discount, rmax, HashingFactory, maxDelta);
		}
		return ramdpLearningAgent.runLearningEpisode(env, maxSteps);
	}
}
