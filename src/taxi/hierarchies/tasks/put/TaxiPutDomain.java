package taxi.hierarchies.tasks.put;

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
import taxi.hierarchies.tasks.nav.NavigateActionType;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import taxi.hierarchies.tasks.put.state.TaxiPutLocation;
import taxi.hierarchies.tasks.put.state.TaxiPutPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiPutDomain implements DomainGenerator {

	public static final String CLASS_PASSENGER =			"PutPassenger";
	public static final String CLASS_TAXI =					"PutTaxi";
	public static final String CLASS_LOCATION =				"PutLocation";

	public static final String ON_ROAD =					"onRoad";

	// attributes
	public static final String ATT_GOAL_LOCATION =			"goalLocation";
	public static final String ATT_TAXI_LOCATION =			"taxiLocation";
	public static final String ATT_IN_TAXI = 				"inTaxi";

	//actions
    public static final String ACTION_NAV =					"nav";
	public static final String ACTION_DROPOFF = 			"dropoff";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a abstraction 2 taxi domain
	 * @param rf reward function
	 * @param tf terminal function
	 */
	public TaxiPutDomain(RewardFunction rf, TerminalFunction tf) {
		this.rf = rf;
		this.tf = tf;
	}
	
	/**
	 * creates a abstraction 2 taxi domain
	 */
	public TaxiPutDomain() {
		this.tf = new TaxiPutTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}

	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_PASSENGER, TaxiPutPassenger.class)
			.addStateClass(CLASS_TAXI, TaxiPutAgent.class)
			.addStateClass(CLASS_LOCATION, TaxiPutLocation.class);

		TaxiPutModel tmodel = new TaxiPutModel();
		FactoredModel model = new FactoredModel(tmodel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes(
				new NavigateActionType(ACTION_NAV, new String[]{CLASS_LOCATION}),
				new DropoffActionType(ACTION_DROPOFF, new String[]{CLASS_PASSENGER})
		);
		
		return domain;
	}

	public static void main(String[] args) {

		TaxiPutDomain taxiBuild = new TaxiPutDomain();
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		PutStateMapper map = new PutStateMapper();
		State L2s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
		Policy p = vi.planFromState(L2s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
