package hierarchies.structureLearning;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;

public class HIMAYTLearningAgent implements LearningAgent {

	private State trainingState;
	private OOSADomain baseDomain;
	private List<StateConditionTest> rootGoal;
	private RAMDPLearningAgent RAMDPLearner;
	private double discount, maxDelta, rmax;
	private int rmaxThreshold;
	private HashableStateFactory hashingFactory;
	
	public HIMAYTLearningAgent(State trainState, OOSADomain baseDomain, List<StateConditionTest> goal,
			double discount, double maxDelta, double rmax, int rmaxThreshold, HashableStateFactory hs) {
		this.trainingState = trainState;
		this.baseDomain = baseDomain;
		this.rootGoal = goal;
		this.discount = discount;
		this.maxDelta = maxDelta;
		this.rmax = rmax;
		this.rmaxThreshold = rmaxThreshold;
		this.hashingFactory = hs;
	}
	
	private Task createHierarchy(){
		return null;
	}
	
	private Task HI_MAT(CATrajectory cat, List<StateConditionTest> goalSet){
		int n = cat.actionCount();
		if(n == 1){
			
		} else if(/*CheckRelVars*/){
			
		}
		
		List<TrajectorySegment> segments = new ArrayList<TrajectorySegment>();
		List<StateConditionTest> openGoalSet = goalSet;
		while(!openGoalSet.isEmpty()){
			StateConditionTest literal = openGoalSet.remove(0);
			TrajectorySegment subtaskSegment = CAT_Scan(cat, literal);
			if(subtaskSegment.getStart() == 0 && subtaskSegment.getEnd() == cat.actionCount() - 1){
				
			}
		}
	}
	
	private TrajectorySegment CAT_Scan(CATrajectory cat, StateConditionTest goal){
		int j = 0;
		State uState = cat.getState(j);
		while(!goal.satisfies(uState)){
			j++;
			uState = cat.getState(j);
		}
		
		int i = j - 1;
		int k;
		boolean actionFound = true;
		while(i >= 0 && actionFound){
			for(Object var : uState.variableKeys()){
				k = cat.findEdge(i, var.toString());
				if(k > j)
					actionFound = false;
			}
			i--;
		}
		return new TrajectorySegment(i, j, goal);
	}
	
	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		if(RAMDPLearner == null){
			Task root = createHierarchy();
			GroundedTask gRoot = root.getAllGroundedTasks(env.currentObservation()).get(0);
			RAMDPLearner = new RAMDPLearningAgent(gRoot, rmaxThreshold, discount, rmax, hashingFactory, maxDelta);
		}
		return RAMDPLearner.runLearningEpisode(env, maxSteps);
	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}
}
