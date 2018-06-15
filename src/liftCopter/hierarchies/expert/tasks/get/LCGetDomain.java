package liftCopter.hierarchies.expert.tasks.get;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
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
import liftCopter.hierarchies.expert.tasks.NavigateActionType;
import liftCopter.hierarchies.expert.tasks.get.state.GetStateMapper;
import liftCopter.hierarchies.expert.tasks.get.state.LCGetAgent;
import liftCopter.hierarchies.expert.tasks.get.state.LCGetLocation;
import liftCopter.hierarchies.expert.tasks.get.state.LCGetCargo;
import taxi.functions.amdp.GetCompletedPF;
import taxi.functions.amdp.GetFailurePF;
import taxi.stateGenerator.TaxiStateFactory;

import static taxi.TaxiConstants.*;

public class LCGetDomain implements DomainGenerator {


    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 taxi domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public LCGetDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 taxi domain
     */
    public LCGetDomain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public LCGetDomain(String goalPassengerName) {
        String[] params = new String[]{goalPassengerName};
        this.tf = new GoalFailTF(new GetCompletedPF(), params, new GetFailurePF(), params);
        this.rf = new GoalFailRF((GoalFailTF) tf);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_PASSENGER, LCGetCargo.class)
            .addStateClass(CLASS_TAXI, LCGetAgent.class)
            .addStateClass(CLASS_LOCATION, LCGetLocation.class);

        LCGetModel tmodel = new LCGetModel();
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
                new NavigateActionType(ACTION_NAV, new String[]{CLASS_LOCATION}),
                new GetPickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER})
        );

        return domain;
    }

    public static void main(String[] args) {

        String goalPassengerName = CLASS_PASSENGER+"0";
        LCGetDomain taxiBuild = new LCGetDomain(goalPassengerName);
        OOSADomain domain = taxiBuild.generateDomain();

        HashableStateFactory hs = new SimpleHashableStateFactory();
        ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);

        State base = TaxiStateFactory.createClassicState();
        GetStateMapper map = new GetStateMapper();
        State L2s = map.mapState(base, new String[]{goalPassengerName});

        SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
        Policy p = vi.planFromState(L2s);
        Episode e = PolicyUtils.rollout(p, env);
        System.out.println(e.actionSequence);
    }

}
