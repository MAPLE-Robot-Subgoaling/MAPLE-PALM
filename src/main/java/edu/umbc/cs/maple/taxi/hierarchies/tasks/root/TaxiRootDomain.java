package edu.umbc.cs.maple.taxi.hierarchies.tasks.root;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.debugtools.RandomFactory;
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
import edu.umbc.cs.maple.config.taxi.TaxiConfig;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.state.hashing.bugfix.BugfixHashableStateFactory;
import edu.umbc.cs.maple.taxi.functions.amdp.RootCompletedPF;
import edu.umbc.cs.maple.taxi.functions.amdp.RootFailurePF;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.RootStateMapper;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.TaxiRootPassenger;
import edu.umbc.cs.maple.utilities.BurlapConstants;

import java.util.Random;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiRootDomain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 taxi domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public TaxiRootDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 taxi domain
     */
    public TaxiRootDomain() {
        this.tf = new GoalFailTF(new RootCompletedPF(), null, new RootFailurePF(), null);
        this.rf = new GoalFailRF((GoalFailTF) tf);
    }


    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_PASSENGER, TaxiRootPassenger.class);

        TaxiRootModel tmodel = new TaxiRootModel();
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
                new GetActionType(ACTION_GET, new String[]{CLASS_PASSENGER}),
                new PutActionType(ACTION_PUT, new String[]{CLASS_PASSENGER})
        );

        return domain;
    }

    public static void main(String[] args) {

        TaxiRootDomain taxiBuild = new TaxiRootDomain();
        OOSADomain domain = taxiBuild.generateDomain();

        HashableStateFactory hs = new BugfixHashableStateFactory(false);
        ValueIteration vi = new ValueIteration(domain, 0.95, hs, 0.00001, 1000);

        Random rng = RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX);
        rng.setSeed(77665544);

        String stateType = "medium-4passengers";
        TaxiConfig taxiConfig = new TaxiConfig();
        taxiConfig.state = stateType;
        State base = taxiConfig.generateState();
//        State base = TaxiStateFactory.createClassicState(2);
        RootStateMapper map = new RootStateMapper();
        State L2s = map.mapState(base);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
        Policy p = vi.planFromState(L2s);
        Episode e = PolicyUtils.rollout(p, env);
        System.out.println(e.stateSequence);
        System.out.println(e.rewardSequence);
        System.out.println(e.actionSequence);
    }

}
