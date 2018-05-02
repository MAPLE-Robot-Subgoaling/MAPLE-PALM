package taxi;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiWall;
import taxi.stateGenerator.TaxiStateFactory;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;

public class Taxi implements DomainGenerator{

	//parameters dictating probabilities of the model
	private RewardFunction rf;
	private TerminalFunction tf;
	private boolean fickle;
	private double fickleProbability;
	private double[][] moveDynamics;
	
	/**
	 * create a taxi domain generator
	 * @param r rewardTotal function
	 * @param t terminal function
	 * @param fickle whether the domain is fickle
	 * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
	 * @param correctMoveprob transitionProbability the taxi will go in the correct direction they select
	 */
	public Taxi(RewardFunction r, TerminalFunction t, boolean fickle,
			double fickleprob, double correctMoveprob) {
		rf = r;
		tf = t;
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		setMoveDynamics(correctMoveprob);
	}
	
	/**
	 * create a taxi domain generator
	 * @param fickle whether the domain is fickle 
	 * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
	 * @param correctMoveprob transitionProbability the taxi will go in the correct direction they select
	 */
	public Taxi(boolean fickle, double fickleprob, double correctMoveprob) {
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		setMoveDynamics(correctMoveprob);
		this.rf = new TaxiRewardFunction();
		this.tf = new TaxiTerminalFunction();
	}
	
	/**
	 * create a taxi domain generator
	 * @param fickle whether the domain is fickle 
	 * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
	 * @param movement a array saying the transitionProbability of execution each action (2nd index) given
	 * the selected action (1rt action)
	 */
	public Taxi(boolean fickle, double fickleprob, double[][] movement) {
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		this.moveDynamics = movement;
		this.rf = new TaxiRewardFunction();
		this.tf = new TaxiTerminalFunction();
	}
	
	/**
	 * creates a non fickle deterministic taxi domain generator
	 */
	public Taxi() {
		this(false, 0, 1);
	}
	
	/**
	 * sets the movement array so the right direction will be taken with
	 * the given transitionProbability and the perpendicular action the rest of the time
	 * @param correctProb the transitionProbability that the correct action is taken
	 */
	private void setMoveDynamics(double correctProb){
		moveDynamics = new double[NUM_MOVE_ACTIONS][NUM_MOVE_ACTIONS];
		
		for(int choose = 0; choose < NUM_MOVE_ACTIONS; choose++){
			for(int outcome = 0; outcome < NUM_MOVE_ACTIONS; outcome++){
				if(choose == outcome){
					moveDynamics[choose][outcome] = correctProb;
				}
				// the two directions which are one away get the rest of prob
				else if(Math.abs(choose - outcome) % 2 == 1){
					moveDynamics[choose][outcome] = (1 - correctProb) / 2;
				}else{
					moveDynamics[choose][outcome] = 0;
				}
			}
		}
	}
	
	
	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_TAXI, TaxiAgent.class).addStateClass(CLASS_PASSENGER, TaxiPassenger.class)
				.addStateClass(CLASS_LOCATION, TaxiLocation.class).addStateClass(CLASS_WALL, TaxiWall.class);
		
		TaxiModel model = new TaxiModel(moveDynamics, fickle, fickleProbability);
		FactoredModel taxiModel = new FactoredModel(model, rf, tf);
		domain.setModel(taxiModel);
		
		domain.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST),
                new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER}),
                new PickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER}));
		
		return domain;
	}
	
	public OOSADomain generateNavigateDomain(){
		OOSADomain d = generateDomain();
		d.clearActionTypes();
		d.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST)
                );
		return d;
	}
				
	public static void main(String[] args) {
		
		Taxi taxiBuild = new Taxi();
		OOSADomain domain = taxiBuild.generateDomain();
				
		HashableStateFactory hs = new SimpleHashableStateFactory();

		State s = TaxiStateFactory.createClassicState();
		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);

		List<Episode> eps = new ArrayList<Episode>();
		QLearning qagent = new QLearning(domain, 0.95, hs, 0, 0.1);
		
		for(int i = 0; i < 1000; i++){
			Episode e = qagent.runLearningEpisode(env, 5000);
			eps.add(e);
			env.resetEnvironment();
		}
		
		EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(5, 5),
				domain, eps);
		v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
		v.initGUI();
	}

}
