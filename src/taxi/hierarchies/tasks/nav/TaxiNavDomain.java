package taxi.hierarchies.tasks.nav;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.nav.state.TaxiNavAgent;
import taxi.hierarchies.tasks.nav.state.TaxiNavLocation;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiNavDomain implements DomainGenerator {

	//public constants for general use
	//object classes
	public static final String CLASS_TAXI = 				"NavTaxi";
	public static final String CLASS_LOCATION = 			"NavLocation";
	public static final String CLASS_WALL =					"NavWall";

	//attributes
	public static final String ATT_X =						"x";
	public static final String ATT_Y =						"y";
	public static final String ATT_GOAL_LOCATION =			"goalLocation";

	// wall attributes
	public static final String ATT_START_X = 				"startX";
	public static final String ATT_START_Y = 				"startY";
	public static final String ATT_IS_HORIZONTAL =			"isHorizontal";
	public static final String ATT_LENGTH =					"length";

	//  action
	public static final String ACTION_NAVIGATE =			"navigate";
	public static final String ACTION_NORTH =				"north";
	public static final String ACTION_SOUTH =				"south";
	public static final String ACTION_EAST =				"east";
	public static final String ACTION_WEST =				"west";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a taxi abstraction 1 domain generator
	 * @param r reward function
	 * @param t terminal function
	 */
	public TaxiNavDomain(RewardFunction r, TerminalFunction t) {
		rf = r;
		tf = t;
	}
	
	/**
	 * create a non fickle taxi abstraction 1 domain
	 */
	public TaxiNavDomain() {
		this.tf = new TaxiNavTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}
	
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_TAXI, TaxiNavAgent.class).addStateClass(CLASS_LOCATION, TaxiNavLocation.class);
		
		TaxiNavModel taxiModel = new TaxiNavModel();
		FactoredModel model = new FactoredModel(taxiModel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes(
				new UniversalActionType(ACTION_NORTH),
				new UniversalActionType(ACTION_SOUTH),
				new UniversalActionType(ACTION_EAST),
				new UniversalActionType(ACTION_WEST)
            );

		return domain;
	}

	public static void main(String[] args) {
		TaxiNavDomain taxiBuild = new TaxiNavDomain();
		OOSADomain domain = taxiBuild.generateDomain();

		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);

		State base = TaxiStateFactory.createClassicState();
		NavStateMapper map = new NavStateMapper();
		State L1s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L1s);
		Policy p = vi.planFromState(L1s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
