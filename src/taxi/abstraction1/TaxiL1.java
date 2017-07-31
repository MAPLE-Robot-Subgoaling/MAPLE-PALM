package taxi.abstraction1;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.abstraction1.state.*;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiL1 implements DomainGenerator {

	//public constants for general use
	//object classes
	public static final String CLASS_L1TAXI = 				"l1Taxi";
	public static final String CLASS_L1PASSENGER =			"l1Passenger";
	public static final String CLASS_L1LOCATION = 			"l1Location";
	
	public static final String ATT_CURRENT_LOCATION = 		"currentLocation";
	//taxi attributes
	public static final String ATT_TAXI_OCCUPIED = 			"taxiOccupied";
	
	//passenger attributes
	public static final String ATT_GOAL_LOCATION = 			"goalLocation";
	public static final String ATT_IN_TAXI = 				"inTaxi";
	public static final String ATT_PICKED_UP_AT_LEAST_ONCE ="pickedUpAtLeastOnce";
	public static final String ATT_JUST_PICKED_UP =			"justPickedUp";
	
	//location attributes 
	public static final String ATT_COLOR =					"color";
	
	//colors
	public static final String COLOR_RED = 					"red";
	public static final String COLOR_YELLOW = 				"yellow";
	public static final String COLOR_GREEN = 				"green";
	public static final String COLOR_BLUE = 				"blue";
	public static final String COLOR_MAGENTA =				"magenta";
	public static final String ON_ROAD =					"onRoad";
	
	//actions
	public static final String ACTION_NAVIGATE =			"navigate";
	public static final String ACTION_L1PICKUP = 			"l1Pickup";
	public static final String ACTION_L1DROPOFF = 			"l1Dropoff";

	//action indexes
	public static int IND_NAVIGATE =						0;
	public static int IND_L1PICKUP = 						1;
	public static int IND_L1DROPOFF = 						2;
	
	private RewardFunction rf;
	private TerminalFunction tf;
	private boolean fickle;
	private double fickleProbability;
	
	/**
	 * creates a taxi abstraction 1 domain generator
	 * @param r reward function
	 * @param t terminal function
	 * @param fickle whether the passengers are fickle
	 * @param fickleprob the probability the passengers change destination
	 */
	public TaxiL1(RewardFunction r, TerminalFunction t, boolean fickle,
			double fickleprob) {
		rf = r;
		tf = t;
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
	}
	
	/**
	 * create a taxi abstraction 1 domain generator
	 * @param fickle whether the passengers are fickle
	 * @param fickleprob the probability the passengers change goal
	 */
	public TaxiL1(boolean fickle, double fickleprob) {
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
		this.tf = new TaxiL1TerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}
	
	/**
	 * create a non fickle taxi abstraction 1 domain
	 */
	public TaxiL1() {
		this(false, 1);
	}
	
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_L1TAXI, TaxiL1Agent.class).addStateClass(CLASS_L1PASSENGER, TaxiL1Passenger.class)
				.addStateClass(CLASS_L1LOCATION, TaxiL1Location.class);
		
		TaxiL1Model taxiModel = new TaxiL1Model(fickle, fickleProbability);
		FactoredModel model = new FactoredModel(taxiModel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes(
				new NavigateActionType(),
				new PickupActionType(),
				new UniversalActionType(ACTION_L1DROPOFF)
				);
		
		return domain;
	}

	//domains for get and put tasks
	public OOSADomain generateGetDomain(){
		OOSADomain d = generateDomain();
		d.clearActionTypes();
		d.addActionTypes(
				new NavigateActionType(),
				new PickupActionType()
				);
		return d;
	}
	
	public OOSADomain generatePutDomain(){
		OOSADomain d = generateDomain();
		d.clearActionTypes();
		d.addActionTypes(
				new NavigateActionType(),
				new UniversalActionType(ACTION_L1DROPOFF)
				);
		return d;
	}

	public static void main(String[] args) {
		
		TaxiL1 taxiBuild = new TaxiL1(false, 0.22);
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		L1StateMapper map = new L1StateMapper();
		State L1s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L1s);
		Policy p = vi.planFromState(L1s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
