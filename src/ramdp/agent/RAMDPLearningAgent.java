package ramdp.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import hierarchy.framework.GroundedTask;
import taxi.Taxi;
import taxi.state.TaxiState;
import utilities.ValueIteration;

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
	 * maximum reward
	 */
	private double rmax;
	
	/**
	 * collection of models for each task
	 */
	private Map<GroundedTask, RAMDPModel> models;
	
	/**
	 * Steps currently taken
	 */
	private int steps;
	
	/**
	 * lookup grounded tasks by name task
	 */
	private Map<String, GroundedTask> taskNames;

	/**
	 * the discount factor
	 */
	private double gamma;
	
	/**
	 * provided state hashing factory
	 */
	private HashableStateFactory hashingFactory;
	
	/**
	 * the max error allowed for the planner
	 */
	private double maxDelta;
	
	/**
	 * the current episode
	 */
	private Episode e;

	private String lastTask;

	private boolean relearn;
	private double relearnFromRoot;
	private int relearnThreshold;
	private int lowerThreshold;
	private int episodeCount = 0;
	/**
	 * create a RAMDP agent on a given task
	 * @param root the root of the hierarchy to learn
	 * @param threshold the rmax sample threshold
	 * @param discount the discount for the tasks' domains 
	 * @param rmax the max reward
	 * @param hs a state hashing factory
	 * @param delta the max error for the planner
	 */
	public RAMDPLearningAgent(GroundedTask root, int threshold, double discount, double rmax,
							  HashableStateFactory hs, double delta, boolean relearn, int relearnThreshold, int lowerThreshold){
		this.relearn = relearn;
		this.rmaxThreshold = threshold;
		this.root = root;
		this.gamma = discount;
		this.hashingFactory = hs;
		this.rmax = rmax;
		this.models = new HashMap<GroundedTask, RAMDPModel>();
		this.taskNames = new HashMap<String, GroundedTask>();
		this.maxDelta = delta;
		this.relearnThreshold=relearnThreshold;
		this.lowerThreshold = lowerThreshold;
	}
	public RAMDPLearningAgent(GroundedTask root, int threshold, double discount, double rmax,
			HashableStateFactory hs, double delta) {
		this(root, threshold, discount, rmax, hs, delta, false, 0, 0);
	}
	
	@Override
	public Episode runLearningEpisode(Environment env) {
		return runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {
		steps = 0;
		lastTask = "";
		e = new Episode(env.currentObservation());
		if(relearn)
            relearnFromRoot = alpha(episodeCount, lowerThreshold, relearnThreshold);
		else
		    relearnFromRoot = 0;
        episodeCount++;
        solveTask(root, env, maxSteps);
		return e;
	}
	private static double alpha(int episodeCount, int lowerThreshold, int relearnThreshold){
	    if(episodeCount<=lowerThreshold)
	        return 0;
	    if(episodeCount<relearnThreshold)
	        return ((double)(episodeCount-lowerThreshold)/(double)(relearnThreshold-lowerThreshold));
	    return 1;
    }
    private static double min(int x, int y){return x<y ? x : y;}

    /**
	 * tries to solve a grounded task while creating a model of it
	 * @param task the grounded task to solve
	 * @param baseEnv a environment defined by the base domain and at the current base state
	 * @param maxSteps the max number of primitive actions that can be taken
	 * @return whether the task was completed 
	 */
	protected boolean solveTask(GroundedTask task, Environment baseEnv, int maxSteps){
		State baseState = e.stateSequence.get(e.stateSequence.size() - 1);
		State pastBaseState = baseState;
		State currentState = task.mapState(baseState);
		State pastState = currentState;
		RAMDPModel model = getModel(task);
		int actionCount = 0;
		boolean earlyterminal = false;
		while( (task.toString().equals("solve")||!earlyterminal ) &&(!(task.isFailure(currentState) || task.isComplete(currentState)) && (steps < maxSteps || maxSteps == -1))){
			actionCount++;
			boolean subtaskCompleted = false;
			pastState = currentState;
			pastBaseState = baseState;
			EnvironmentOutcome result;

			Action a = nextAction(task, currentState);
			GroundedTask action = this.taskNames.get(a.actionName());
			if(action == null){
				addChildrenToMap(task, currentState);
				action = this.taskNames.get(a.actionName());
			}

			if(action.isPrimitive()){
				subtaskCompleted = true;
				result = baseEnv.executeAction(a);
				e.transition(result);
				baseState = result.op;
				currentState = task.mapState(result.op);
				result.o = pastState;
				result.op = currentState;
				result.a = a;
				result.r = task.getReward(currentState);
				steps++;
			}else{
				subtaskCompleted = solveTask(action, baseEnv, maxSteps);
				baseState = e.stateSequence.get(e.stateSequence.size() - 1);
				currentState = task.mapState(baseState);

				result = new EnvironmentOutcome(pastState, a, currentState,
						task.getReward(currentState), task.isFailure
						(currentState));
			}
			
			//update task model if the subtask completed correctly
			if(subtaskCompleted){
				model.updateModel(result);
			}
//			String goalColor = (String) ((TaxiState)pastBaseState).getPassengerAtt(Taxi.CLASS_PASSENGER+"0", Taxi.ATT_GOAL_LOCATION);
			//if(!goalColor.equals(Taxi.COLOR_RED)) {
//				System.out.println("Task: "+task.toString());
//				System.out.println("Goal color: "+goalColor);
//				System.out.println("state-action count: " + model.getStateActionCount(hashingFactory.hashState(pastState), a));
//				System.out.println("subtask complete: "+subtaskCompleted);
			//}
            if(relearn)
                if(model.getStateActionCount(this.hashingFactory.hashState(pastState),a)
                        > rmaxThreshold){
                    earlyterminal = randomRelearn();
                }else
                    earlyterminal = false;

			//earlyterminal = randomRelearn();
		}
//		System.out.println("task: "+task.toString());
//		if(task.toString().equals(lastTask))
//			System.out.println("Double action: "+task);
//		else if(task.toString().startsWith("navigate")&&lastTask.startsWith("navigate"))
//			System.out.println("Double nav: "+lastTask+", "+task);
//		lastTask = task.toString();
		//System.out.println(task + " " + actionCount);
		//if(task.toString().startsWith("navigate"))
		return (task.isComplete(currentState)||earlyterminal) || actionCount == 0;
	}
	private boolean randomRelearn(){
		Random rand = new Random();
		return rand.nextDouble()<this.relearnFromRoot;
	}
	/**
	 * add the children of the given task to the action name lookup
	 * @param gt the current grounded task
	 * @param s the current state
	 */
	protected void addChildrenToMap(GroundedTask gt, State s){
		List<GroundedTask> chilkdren = gt.getGroundedChildTasks(s);
		for(GroundedTask child : chilkdren){
			taskNames.put(child.toString(), child);
		}
	}
	
	/**
	 * plan over the given task's model and pick the best action to do next favoring unmodeled actions
	 * @param task the current task
	 * @param s the current state
	 * @return the best action to take
	 */
	protected Action nextAction(GroundedTask task, State s){
		RAMDPModel model = getModel(task);
		OOSADomain domain = task.getDomain(model);
		ValueIteration plan = new ValueIteration(domain, gamma, hashingFactory, maxDelta, 1000);
		Policy viPolicy = plan.planFromState(s);
		Policy rmaxPolicy = new RMAXPolicy(model, viPolicy, domain.getActionTypes(), hashingFactory);
		
		return rmaxPolicy.action(s);
	}

	/**
	 * get the rmax model of the given task
	 * @param t the current task
	 * @return the learned rmax model of the task
	 */
	protected RAMDPModel getModel(GroundedTask t){
		RAMDPModel model = models.get(t);
		if(model == null){
			model = new RAMDPModel(t, this.rmaxThreshold, this.rmax, this.hashingFactory);
			this.models.put(t, model);
		}
		return model;
	}
}
