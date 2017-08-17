package taxi.hierarchies.tasks.root;

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
import taxi.hierarchies.tasks.root.state.RootStateMapper;
import taxi.hierarchies.tasks.root.state.TaxiRootLocation;
import taxi.hierarchies.tasks.root.state.TaxiRootPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiRootDomain implements DomainGenerator {

	public static final String CLASS_PASSENGER =			"RootPassenger";
	public static final String CLASS_LOCATION = 			"RootLocation";
	
	public static final String ATT_CURRENT_LOCATION = 		"currentLocation";
	
	//passenger attributes
	public static final String ATT_IN_TAXI = 				"inTaxi";
	public static final String ATT_GOAL_LOCATION =			"goalLocation";

	//location attributes 
	public static final String ATT_COLOR =					"color";

	//actions
	public static final String ACTION_SOLVE = 				"solve";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a abstraction 2 taxi domain
	 * @param rf reward function
	 * @param tf terminal function
	 */
	public TaxiRootDomain(RewardFunction rf, TerminalFunction tf, boolean fickle, double fickleprob) {
		this.rf = rf;
		this.tf = tf;
	}
	
	/**
	 * creates a abstraction 2 taxi domain
	 */
	public TaxiRootDomain() {
		this.tf = new TaxiRootTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}

	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_PASSENGER, TaxiRootPassenger.class)
			.addStateClass(CLASS_LOCATION, TaxiRootLocation.class);
		
		TaxiRootModel tmodel = new TaxiRootModel();
		FactoredModel model = new FactoredModel(tmodel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes( new UniversalActionType(ACTION_SOLVE) );
		
		return domain;
	}

	public static void main(String[] args) {

		TaxiRootDomain taxiBuild = new TaxiRootDomain();
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		RootStateMapper map = new RootStateMapper();
		State L2s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
		Policy p = vi.planFromState(L2s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
