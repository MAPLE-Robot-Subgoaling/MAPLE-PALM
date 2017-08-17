package taxi.hierarchies.tasks.get;

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
import taxi.hierarchies.tasks.get.state.GetStateMapper;
import taxi.hierarchies.tasks.get.state.TaxiGetLocation;
import taxi.hierarchies.tasks.get.state.TaxiGetPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiGetDomain implements DomainGenerator {

	public static final String CLASS_PASSENGER =			"GetPassenger";
	public static final String CLASS_LOCATION = 			"GetLocation";
	
	public static final String ATT_CURRENT_LOCATION = 		"currentLocation";
	
	//passenger attributes
	public static final String ATT_IN_TAXI = 				"inTaxi";

	//location attributes 
	public static final String ATT_COLOR =					"color";

	//actions
	public static final String ACTION_GET = 				"get";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a abstraction 2 taxi domain
	 * @param rf reward function
	 * @param tf terminal function
	 */
	public TaxiGetDomain(RewardFunction rf, TerminalFunction tf, boolean fickle, double fickleprob) {
		this.rf = rf;
		this.tf = tf;
	}
	
	/**
	 * creates a abstraction 2 taxi domain
	 */
	public TaxiGetDomain() {
		this.tf = new TaxiGetTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}

	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_PASSENGER, TaxiGetPassenger.class)
			.addStateClass(CLASS_LOCATION, TaxiGetLocation.class);
		
		TaxiGetModel tmodel = new TaxiGetModel();
		FactoredModel model = new FactoredModel(tmodel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes( new GetActionType() );
		
		return domain;
	}

	public static void main(String[] args) {

		TaxiGetDomain taxiBuild = new TaxiGetDomain();
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		GetStateMapper map = new GetStateMapper();
		State L2s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
		Policy p = vi.planFromState(L2s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
