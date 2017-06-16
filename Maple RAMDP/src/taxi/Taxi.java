package taxi;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
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
import taxi.state.TaxiStateFactory;
import taxi.state.TaxiWall;

public class Taxi implements DomainGenerator{

	//public constants for general use
	//object classes
	public static final String CLASS_TAXI = 				"Taxi";
	public static final String CLASS_PASSENGER =			"Passenger";
	public static final String CLASS_LOCATION = 			"Location";
	public static final String CLASS_WALL = 				"Wall";
	
	//common attributes
	public static final String ATT_X =						"x";
	public static final String ATT_Y =						"y";
	
	//taxi attributes
	public static final String ATT_TAXI_OCCUPIED = 			"taxiOccupied";
	
	//passenger attributes
	public static final String ATT_GOAL_LOCATION = 			"goalLocation";
	public static final String ATT_IN_TAXI = 				"inTaxi";
	public static final String ATT_JUST_PICKED_UP = 		"justPickedUp";
	public static final String ATT_PICKED_UP_AT_LEAST_ONCE ="pickedUpAtLeastOnce";
	
	//location attributes 
	public static final String ATT_COLOR =					"color";
	
	//wall attributes
	public static final String ATT_START_X = 				"startX";
	public static final String ATT_START_Y = 				"startY";
	public static final String ATT_LENGTH = 				"length";
	public static final String ATT_IS_HORIZONTAL =			"isHorizontal";
	
	//colors
	public static final String COLOR_RED = 					"red";
	public static final String COLOR_YELLOW = 				"yellow";
	public static final String COLOR_GREEN = 				"green";
	public static final String COLOR_GLUE = 				"blue";
	
	//actions
	public static final int NUM_MOVE_ACTIONS = 				4;
	public static final String ACTION_NORTH = 				"north";
	public static final String ACTION_EAST =				"east";
	public static final String ACTION_SOUTH =				"south";
	public static final String ACTION_WEST = 				"west";
	public static final String ACTION_PICKUP = 				"pickup";
	public static final String ACTION_DROPOFF = 			"dropoff";
	
	public static int IND_NORTH = 							0;
	public static int IND_EAST = 							1;
	public static int IND_SOUTH = 							2;
	public static int IND_WEST = 							3;
	public static int IND_PICKUP = 							4;
	public static int IND_DROPOFF = 						5;
	
	private RewardFunction rf;
	private TerminalFunction tf;
	private boolean fickle;
	private double fickleProbability;
	private double[][] moveDynamics;
	
	public Taxi(RewardFunction r, TerminalFunction t, boolean fickle,
			double fickleprob, double correctMoveprob) {
		rf = r;
		tf = t;
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		setMoveDynamics(correctMoveprob);
	}
	
	//fickle deterministic
	public Taxi(boolean fickle, double fickleprob, double correctMoveprob) {
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		setMoveDynamics(correctMoveprob);
		this.rf = new TaxiRewardFunction();
		this.tf = new TaxiTerminalFunction();
	}
	
	public Taxi(boolean fickle, double fickleprob, double[][] movement) {
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		this.moveDynamics = movement;
		this.rf = new TaxiRewardFunction();
		this.tf = new TaxiTerminalFunction();
	}
	
	public Taxi() {
		this.rf = new TaxiRewardFunction();
		this.tf = new TaxiTerminalFunction();
		this.fickle = false;
		this.fickleProbability = 0;
		setMoveDynamics(1);
	}
	
	private void setMoveDynamics(double correctProb){
		moveDynamics = new double[NUM_MOVE_ACTIONS][NUM_MOVE_ACTIONS];
		
		for(int choose = 0; choose < NUM_MOVE_ACTIONS; choose++){
			for(int outcome = 0; outcome < NUM_MOVE_ACTIONS; outcome++){
				if(choose == outcome){
					moveDynamics[choose][outcome] = correctProb;
				}
				// the two directions which are one away get the rest of prob
				else if(Math.abs(choose - outcome) == 1){
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
                new UniversalActionType(ACTION_DROPOFF),
                new UniversalActionType(ACTION_PICKUP));
		
		return domain;
	}
	
	public static void main(String[] args) {
		
		Taxi taxiBuild = new Taxi();
		OOSADomain domain = taxiBuild.generateDomain();
				
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.95, hs, 0.01, 10);
		
		State s = TaxiStateFactory.createClassicState();
		Policy p = vi.planFromState(s);
		
		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		Episode e = PolicyUtils.rollout(p, env);
		
		List<Episode> eps = new ArrayList<Episode>();
		eps.add(e);
		EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(),
				domain, eps);
		v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
		v.initGUI();
	}

}
