package taxi.hierarchies.tasks.nav;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GoalFailRF;
import hierarchy.framework.GoalFailTF;
import taxi.functions.amdp.NavCompletedPF;
import taxi.functions.amdp.NavFailurePF;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.nav.state.TaxiNavAgent;
import taxi.hierarchies.tasks.nav.state.TaxiNavLocation;
import taxi.stategenerator.TaxiStateFactory;

import static taxi.TaxiConstants.*;

public class TaxiNavDomain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a taxi abstraction 1 domain generator
     * @param r rewardTotal function
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
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public TaxiNavDomain(String goalLocationName) {
        tf = new GoalFailTF(new NavCompletedPF(), new String[]{goalLocationName}, new NavFailurePF(), null);
        rf = new GoalFailRF((GoalFailTF) tf);
    }

    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_TAXI, TaxiNavAgent.class).addStateClass(CLASS_LOCATION, TaxiNavLocation.class);

        TaxiNavModel taxiModel = new TaxiNavModel();
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

        domain.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST)
            );

        return domain;
    }

    public static void main(String[] args) {

        String goalLocationName = CLASS_LOCATION+"2";
        TaxiNavDomain taxiBuild = new TaxiNavDomain(goalLocationName);
        OOSADomain domain = taxiBuild.generateDomain();

        HashableStateFactory hs = new SimpleHashableStateFactory();
        ValueIteration vi = new ValueIteration(domain, 0.99, hs, 0.0001, 1000);

        State base = TaxiStateFactory.createClassicState();
        NavStateMapper map = new NavStateMapper();
        State L1s = map.mapState(base);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, L1s);
        Policy p = vi.planFromState(L1s);
        Episode e = PolicyUtils.rollout(p, env);
        System.out.println(e.actionSequence);
    }

}
