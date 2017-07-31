package ramdp.agent;

import java.util.*;

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
	private int actionCount = 0;
	private int randomReplanCount = 0;
	private int randomNoReplanCount = 0;
	private int autoterminalCount = 0;
	public String goal;
	public String start;
    //private Map<GroundedTask, Integer> taskRelearnCount;
    private Map<String,Map<String, Integer>> firstFewAC = new HashMap<>();
    private Map<String,Map<String, Integer>> firstFewRRC =  new HashMap<>();
    private Map<String,Map<String, Integer>> firstFewRNRC = new HashMap<>();
    private Map<String,Map<String, Integer>> firstFewATC = new HashMap<>();


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

//        if( this.actionCount<this.lowerThreshold||(episodeCount%5 == 0 ||
//            this.actionCount < this.relearnThreshold &&
//            this.actionCount >(this.relearnThreshold /2)))



        solveTask(root, env, maxSteps);
        episodeCount++;
        System.out.println("Action count: "+this.actionCount);
        System.out.println("Random replan count: "+this.randomReplanCount);
        System.out.println("Random noreplan count: "+this.randomNoReplanCount);
        System.out.println("Autoterminal count: "+this.autoterminalCount);
        if(episodeCount<=32){
            prepMap(this.firstFewRRC);
            prepMap(this.firstFewAC);
            prepMap(this.firstFewRNRC);
            prepMap(this.firstFewATC);

            writeMap(this.firstFewRRC, this.randomReplanCount);
            writeMap(this.firstFewRNRC,this.randomNoReplanCount);
            writeMap(this.firstFewATC,this.autoterminalCount);
            writeMap(this.firstFewAC,this.actionCount);
            if(episodeCount==32){
                    first32();
            }
        }


        this.randomReplanCount =0;
        this.randomNoReplanCount=0;
        this.autoterminalCount=0;
        this.actionCount=0;
		return e;
	}
    private void prepMap(Map<String, Map<String, Integer>> mp){
        mp.putIfAbsent(goal, new HashMap<>());
        mp.get(goal).putIfAbsent(start, 0);
    }
    private void writeMap(Map<String, Map<String, Integer>> mp, int value){
        int val = mp.get(goal).get(start);
        mp.get(goal).put(start,val+value);
    }
    public void first32(){
        System.out.println("@@@@@@@ FIRST 32 EPISODE COUNTS @@@@@@@");
        System.out.println("ACTION COUNTS");
        printMap(this.firstFewAC);
        System.out.println("RANDOM REPLAN COUNTS");
        printMap(this.firstFewRRC);
        System.out.println("RANDOM NOREPLAN COUNTS");
        printMap(this.firstFewRNRC);
        System.out.println("AUTOTERMINAL COUNTS");
        printMap(this.firstFewATC);


    }
    private void printMap(Map<String,Map<String, Integer>> mp){
        for(Map.Entry<String, Map<String, Integer>> e : mp.entrySet()){
            System.out.println("Pass goal is "+e.getKey());
            for(Map.Entry<String, Integer> ie : e.getValue().entrySet())
                System.out.println("Pass start is "+ie.getKey()+", count is "+ie.getValue());
        }
    }


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
		boolean relearnterminal = false;
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
				this.actionCount++;
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

            if(relearn) {

//			    List<ActionType> actionTypes= task.getDomain().getActionTypes();
//			    int localConverge = 0;
//			    int convergeSum = 0;
//                for(ActionType type : actionTypes)
//                    for (Action act : type.allApplicableActions(pastState)) {
//                        localConverge += rmaxThreshold;
//                        convergeSum += min(model.getStateActionCount(this.hashingFactory.hashState(pastState), act), rmaxThreshold);
//                    }
//                System.out.println("######################");
//                int localCount = (model.getStateActionCount(this.hashingFactory.hashState(pastState),a));
//                System.out.println("Superlocally coverged: "+(localCount>=rmaxThreshold)+" "+localCount+" "+rmaxThreshold);
//                System.out.println("Locally converged: "+(convergeSum>=localConverge)+" "+convergeSum+" "+localConverge);

                if(relearnterminal)
                    autoterminalCount++;
                if ((!relearnterminal) && model.getStateActionCount(this.hashingFactory.hashState(pastState), a)
                        >= rmaxThreshold) {
//                if(convergeSum>=localConverge){
                    earlyterminal = randomRelearn();
                    if (earlyterminal)
                        randomReplanCount++;
                    else
                        randomNoReplanCount++;
                }else {
                    earlyterminal = false;
                    relearnterminal = true;
                }

//
            }
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
        if(relearn)
            relearnFromRoot = alpha(episodeCount, lowerThreshold, relearnThreshold);
        else
            relearnFromRoot = 0;
		return rand.nextDouble()<this.relearnFromRoot;
	}
    private static double alpha(int count, int lowerThreshold, int relearnThreshold){
	    int halfThreshold = relearnThreshold/2;
        if(count<=lowerThreshold)
            return 0;
//        if(count<=halfThreshold)
//            return ((double)(count-lowerThreshold)/(double)(relearnThreshold-lowerThreshold));
//        if(count<relearnThreshold)
//            return ((double)(1/2)+(double)((2*count)-relearnThreshold)/relearnThreshold);
        if(count<relearnThreshold)
            return ((double)(count-lowerThreshold)/(double)(relearnThreshold-lowerThreshold));
        return 1;
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
