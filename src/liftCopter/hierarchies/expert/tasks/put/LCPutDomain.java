package liftCopter.hierarchies.expert.tasks.put;

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
import liftCopter.LiftCopter;
import liftCopter.hierarchies.expert.tasks.NavigateActionType;
import liftCopter.hierarchies.expert.tasks.put.state.PutStateMapper;
import liftCopter.hierarchies.expert.tasks.put.state.LCPutAgent;
import liftCopter.hierarchies.expert.tasks.put.state.LCPutLocation;
import liftCopter.hierarchies.expert.tasks.put.state.LCPutCargo;
import liftCopter.hierarchies.functions.PutCompletedPF;
import liftCopter.hierarchies.functions.PutFailurePF;
import liftCopter.stateGenerator.LiftCopterStateFactory;

import static liftCopter.LiftCopterConstants.*;

public class LCPutDomain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 agent domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public LCPutDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 agent domain
     */
    public LCPutDomain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public LCPutDomain(String goalCargoName) {
        String[] params = new String[]{goalCargoName};
        this.tf = new GoalFailTF(new PutCompletedPF(), params, new PutFailurePF(), params);
        this.rf = new GoalFailRF((GoalFailTF) tf);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_CARGO, LCPutCargo.class)
            .addStateClass(CLASS_AGENT, LCPutAgent.class)
            .addStateClass(CLASS_LOCATION, LCPutLocation.class);

        LCPutModel tmodel = new LCPutModel();
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
                new PutPutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_CARGO})
        );

        return domain;
    }

    public static void main(String[] args) {

        String goalCargoName = CLASS_CARGO+"0";
        LCPutDomain agentBuild = new LCPutDomain(goalCargoName);
        OOSADomain domain = agentBuild.generateDomain();

        HashableStateFactory hs = new SimpleHashableStateFactory();
        ValueIteration vi = new ValueIteration(domain, 0.5, hs, 0.01, 10);

        State base = LiftCopterStateFactory.createClassicStateHalfpoint(true);
        PutStateMapper map = new PutStateMapper();
        State L2s = map.mapState(base, new String[]{goalCargoName});

        SimulatedEnvironment env = new SimulatedEnvironment(domain, L2s);
        Policy p = vi.planFromState(L2s);
        Episode e = PolicyUtils.rollout(p, env);
        System.out.println(e.actionSequence);
    }

}
