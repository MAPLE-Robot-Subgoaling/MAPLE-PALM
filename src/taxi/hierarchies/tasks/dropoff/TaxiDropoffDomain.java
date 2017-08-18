package taxi.hierarchies.tasks.dropoff;

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
import taxi.PutdownActionType;
import taxi.hierarchies.tasks.dropoff.state.DropoffStateMapper;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffAgent;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffLocation;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiDropoffDomain implements DomainGenerator {

	//public constants for general use
	//object classes
	public static final String CLASS_TAXI = 				"DropoffTaxi";
	public static final String CLASS_PASSENGER =			"DropoffPassenger";
	public static final String CLASS_LOCATION = 			"DropoffLocation";

	// attributes
	public static final String ATT_CURRENT_LOCATION = 		"currentLocation";
	public static final String ATT_TAXI_OCCUPIED = 			"taxiOccupied";
	public static final String ATT_IN_TAXI =				"inTaxi";
	public static final String ATT_COLOR =					"color";
	
	public static final String ON_ROAD =					"onRoad";
	
	//actions
	public static final String ACTION_DROPOFF = 			"dropoff";

	private RewardFunction rf;
	private TerminalFunction tf;

	/**
	 * creates a taxi abstraction 1 domain generator
	 * @param r reward function
	 * @param t terminal function
	 */
	public TaxiDropoffDomain(RewardFunction r, TerminalFunction t) {
		rf = r;
		tf = t;
	}
	
	/**
	 * create a taxi abstraction 1 domain generator
	 */
	public TaxiDropoffDomain() {
		this.tf = new TaxiDropoffTerminalFunction();
		this.rf = new GoalBasedRF(tf);
	}
	
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(CLASS_TAXI, TaxiDropoffAgent.class).addStateClass(CLASS_PASSENGER, TaxiDropoffPassenger.class)
				.addStateClass(CLASS_LOCATION, TaxiDropoffLocation.class);
		
		TaxiDropoffModel taxiModel = new TaxiDropoffModel();
		FactoredModel model = new FactoredModel(taxiModel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes( new PutdownActionType() );
		
		return domain;
	}

	public static void main(String[] args) {
		
		TaxiDropoffDomain taxiBuild = new TaxiDropoffDomain();
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicState();
		DropoffStateMapper map = new DropoffStateMapper();
		State s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		Policy p = vi.planFromState(s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
