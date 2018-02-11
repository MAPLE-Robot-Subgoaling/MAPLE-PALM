package taxi.hierarchies.tasks.dropoff;

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
import taxi.functions.amdp.DropoffCompletedPF;
import taxi.functions.amdp.DropoffFailurePF;
import taxi.hierarchies.tasks.dropoff.state.DropoffStateMapper;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffPassenger;
import taxi.stateGenerator.TaxiStateFactory;

public class TaxiDropoffDomain implements DomainGenerator {

	public static final String ON_ROAD =					"onRoad";
	public static final String NOT_IN_TAXI =				"notInTaxi";

	// attributes
	public static final String ATT_LOCATION = 				"currentLocation";

	// actions
	public static final String ACTION_PUTDOWN =				"putdown";

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

	public TaxiDropoffDomain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
	}
	
	/**
	 * create a taxi abstraction 1 domain generator
	 */
	public TaxiDropoffDomain(String goalPassengerName) {
		String[] params = new String[]{goalPassengerName};
		this.tf = new GoalFailTF(new DropoffCompletedPF(), params, new DropoffFailurePF(), params);
		this.rf = new GoalFailRF((GoalFailTF) tf);
	}
	
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();
		
		domain.addStateClass(Taxi.CLASS_PASSENGER, TaxiDropoffPassenger.class);
		
		TaxiDropoffModel taxiModel = new TaxiDropoffModel();
        if (tf == null) {
            System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null TF");
            tf = new NullTermination();
        }
        if (rf == null) {
            System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null RF");
            rf = new NullRewardFunction();
        }
		FactoredModel model = new FactoredModel(taxiModel, rf, tf);
		domain.setModel(model);
		
		domain.addActionTypes( new DropoffPutdownActionType(ACTION_PUTDOWN, new String[]{Taxi.CLASS_PASSENGER}) );
		
		return domain;
	}

	public static void main(String[] args) {
		String goalPassengerName = Taxi.CLASS_PASSENGER+"0";
		TaxiDropoffDomain taxiBuild = new TaxiDropoffDomain(goalPassengerName);
		OOSADomain domain = taxiBuild.generateDomain();
		
		HashableStateFactory hs = new SimpleHashableStateFactory();
		ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);
		
		State base = TaxiStateFactory.createClassicStateHalfpoint(true);
		DropoffStateMapper map = new DropoffStateMapper();
		State s = map.mapState(base);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
		Policy p = vi.planFromState(s);
		Episode e = PolicyUtils.rollout(p, env);
		System.out.println(e.actionSequence);
	}

}
