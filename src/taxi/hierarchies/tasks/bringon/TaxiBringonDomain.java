package taxi.hierarchies.tasks.bringon;

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
import taxi.hierarchies.tasks.bringon.state.*;
import taxi.stateGenerator.TaxiStateFactory;
import taxi.Taxi;

public class TaxiBringonDomain implements DomainGenerator {

	public static final String ON_ROAD =					"onRoad";
	public static final String IN_TAXI =					"inTaxi";

	// attributes
	public static final String ATT_LOCATION = 				"location";

	// actions
	public static final String ACTION_PICKUP =				"pickup";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a taxi abstraction 1 domain generator
	 * @param r reward function
	 * @param t terminal function
	 */
	public TaxiBringonDomain(RewardFunction r, TerminalFunction t) {
		rf = r;
		tf = t;
	}
	
	/**
	 * create a taxi abstraction 1 domain generator
	 */
	public TaxiBringonDomain() {
		this.tf = new TaxiBringonTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}
	
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(Taxi.CLASS_TAXI, TaxiBringonAgent.class).addStateClass(Taxi.CLASS_PASSENGER, TaxiBringonPassenger.class);

		TaxiBringonModel taxiModel = new TaxiBringonModel();
		FactoredModel model = new FactoredModel(taxiModel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes( new PickupActionType(ACTION_PICKUP, new String[]{Taxi.CLASS_PASSENGER}) );
		
		return domain;
	}

	public static void main(String[] args) {
		TaxiBringonDomain taxiBuild = new TaxiBringonDomain();
		OOSADomain domain = taxiBuild.generateDomain();

		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);

		State base = TaxiStateFactory.createClassicState();
		BringonStateMapper map = new BringonStateMapper();
		State s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		Policy p = vi.planFromState(s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
