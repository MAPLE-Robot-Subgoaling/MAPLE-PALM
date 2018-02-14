package taxi.hierarchies.tasks.put;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GoalFailRF;
import hierarchy.framework.GoalFailTF;
import taxi.Taxi;
import taxi.functions.amdp.PutCompletedPF;
import taxi.functions.amdp.PutFailurePF;
import taxi.hierarchies.tasks.put.state.PutStateMapper;
import taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import taxi.hierarchies.tasks.put.state.TaxiPutLocation;
import taxi.hierarchies.tasks.put.state.TaxiPutPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiPutDomain implements DomainGenerator {

	public static final String ON_ROAD =					"onRoad";
	public static final String IN_TAXI = 					"inTaxi";

	// attributes
	public static final String ATT_GOAL_LOCATION =			"goalLocation";
	public static final String ATT_TAXI_LOCATION =			"taxiLocation";
	public static final String ATT_LOCATION =				"location";

	//actions
    public static final String ACTION_NAV =					"nav";
	public static final String ACTION_DROPOFF = 			"dropoff";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a abstraction 2 taxi domain
	 * @param rf rewardTotal function
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
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
	}

	public TaxiPutDomain(String goalPassengerName) {
		String[] params = new String[]{goalPassengerName};
		this.tf = new GoalFailTF(new PutCompletedPF(), params, new PutFailurePF(), params);
		this.rf = new GoalFailRF((GoalFailTF) tf);
	}

	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(Taxi.CLASS_PASSENGER, TaxiPutPassenger.class)
			.addStateClass(Taxi.CLASS_TAXI, TaxiPutAgent.class)
			.addStateClass(Taxi.CLASS_LOCATION, TaxiPutLocation.class);

		TaxiPutModel tmodel = new TaxiPutModel();
		if (tf == null) {
			System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null TF");
			tf = new NullTermination();
		}
		if (rf == null) {
			System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null RF");
			rf = new NullRewardFunction();
		}
		FactoredModel model = new FactoredModel(tmodel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes(
				new NavigateActionType(ACTION_NAV, new String[]{Taxi.CLASS_LOCATION}),
				new DropoffActionType(ACTION_DROPOFF, new String[]{Taxi.CLASS_PASSENGER})
		);
		
		return domain;
	}

	public static void main(String[] args) {

		String goalPassengerName = Taxi.CLASS_PASSENGER+"0";
		TaxiPutDomain taxiBuild = new TaxiPutDomain(goalPassengerName);
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicStateHalfpoint(true);
		PutStateMapper map = new PutStateMapper();
		State L2s = map.mapState(base, new String[]{goalPassengerName});

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
		Policy p = vi.planFromState(L2s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
