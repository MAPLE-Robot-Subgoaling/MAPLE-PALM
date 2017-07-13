package taxi.abstraction2;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import taxi.abstraction2.state.L2StateMapper;
import taxi.abstraction2.state.TaxiL2Location;
import taxi.abstraction2.state.TaxiL2Passenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiL2 implements DomainGenerator {

	public static final String CLASS_L2PASSENGER =			"l2Passenger";
	public static final String CLASS_L2LOCATION = 			"l2Location";
	
	public static final String ATT_CURRENT_LOCATION = 		"currentLocation";
	
	//passenger attributes
	public static final String ATT_GOAL_LOCATION = 			"goalLocation";
	public static final String ATT_IN_TAXI = 				"inTaxi";
	public static final String ATT_PICKED_UP_AT_LEAST_ONCE ="pickedUpAtLeastOnce";
	//public static final String ATT_JUST_PICKED_UP =			"justPickedUp";
	
	//location attributes 
	public static final String ATT_COLOR =					"color";
	
	//colors
	public static final String COLOR_RED = 					"red";
	public static final String COLOR_YELLOW = 				"yellow";
	public static final String COLOR_GREEN = 				"green";
	public static final String COLOR_BLUE = 				"blue";
	public static final String COLOR_MAGENTA =				"magenta";
	
	//actions
	public static final String ACTION_GET = 				"get";
	public static final String ACTION_PUT = 				"put";
	
	public static int IND_GET =								0;
	public static int IND_PUT = 							1;
	
	private RewardFunction rf;
	private TerminalFunction tf;
	private boolean fickle;
	private double fickleProbability;

	/**
	 * creates a abstraction 2 taxi domain
	 * @param rf reward function
	 * @param tf terminal function
	 * @param fickle whether the domain is fickle
	 * @param fickleprob the probability the passenger will change their goal
	 */
	public TaxiL2(RewardFunction rf, TerminalFunction tf, boolean fickle, double fickleprob) {
		this.rf = rf;
		this.tf = tf;
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
	}
	
	/**
	 * creates a abstraction 2 taxi domain
	 * @param fickle whether the domain is fickle
	 * @param fickleprob the probability the passenger will change their goal
	 */
	public TaxiL2(boolean fickle, double fickleprob) {
		this.tf = new TaxiL2TerminalFunction(); 
		this.rf = new GoalBasedRF(tf);
		this.fickle = fickle;
		this.fickleProbability = fickleprob;
	}

	/**
	 * create default taxi l2 non fickle domain
	 */
	public TaxiL2() {
		this.tf = new TaxiL2TerminalFunction(); 
		this.rf = new GoalBasedRF(tf);
		this.fickle = false;
		this.fickleProbability = 0;
	}
	
	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_L2PASSENGER, TaxiL2Passenger.class)
			.addStateClass(CLASS_L2LOCATION, TaxiL2Location.class);
		
		TaxiL2Model tmodel = new TaxiL2Model(fickle, fickleProbability);
		FactoredModel model = new FactoredModel(tmodel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes(
				new GetActionType(),
				new PutActionType()
				);
		
		return domain;
	}

	public static void main(String[] args) {

		TaxiL2 taxiBuild = new TaxiL2(true, 0.5);  
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		L2StateMapper map = new L2StateMapper();
		State L2s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
		Policy p = vi.planFromState(L2s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
