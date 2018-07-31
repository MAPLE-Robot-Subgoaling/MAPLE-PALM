package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav;

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
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.liftCopter.ThrustType;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavAgent;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavLocation;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.NavStateMapper;
import edu.umbc.cs.maple.liftCopter.hierarchies.functions.NavCompletedPF;
import edu.umbc.cs.maple.liftCopter.hierarchies.functions.NavFailurePF;
import edu.umbc.cs.maple.liftCopter.stateGenerator.LiftCopterStateFactory;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class LCNavDomain implements DomainGenerator {
    private RewardFunction rf;
    private TerminalFunction tf;
    public List<Double> thrustValues = new ArrayList();
    public List<Double> directionValues = new ArrayList();
    /**
     * creates a taxi abstraction 1 domain generator
     * @param r rewardTotal function
     * @param t terminal function
     */
    public LCNavDomain(RewardFunction r, TerminalFunction t) {
        rf = r;
        tf = t;
    }

    /**
     * create a non fickle taxi abstraction 1 domain
     */
    public LCNavDomain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public LCNavDomain(String goalLocationName) {
        tf = new GoalFailTF(new NavCompletedPF(), new String[]{goalLocationName}, new NavFailurePF(), null);
        rf = new GoalFailRF((GoalFailTF) tf);
    }

    public void addStandardThrustValues() {
        this.thrustValues.add(0.02D);
    }
    public void addStandardThrustDirections() {
        this.directionValues.add(0.0);
        this.directionValues.add(0.5*Math.PI);
        this.directionValues.add(Math.PI);
        this.directionValues.add(1.5*Math.PI);
    }
    public List<Double> getThrustValues(){
        return thrustValues;
    }
    public List<Double> getDirectionValues(){
        return directionValues;
    }
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_AGENT, LCNavAgent.class).addStateClass(CLASS_LOCATION, LCNavLocation.class);

        LCNavModel taxiModel = new LCNavModel();
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

        addStandardThrustDirections();
        addStandardThrustValues();
        domain.addActionTypes(new UniversalActionType(ACTION_IDLE))
                .addActionTypes(new ThrustType(this.thrustValues, this.directionValues));

        return domain;
    }

    public static void main(String[] args) {

        String goalLocationName = CLASS_LOCATION+"2";
        LCNavDomain taxiBuild = new LCNavDomain(goalLocationName);
        OOSADomain domain = taxiBuild.generateDomain();

        HashableStateFactory hs = new SimpleHashableStateFactory();
        ValueIteration vi = new ValueIteration(domain, 0.99, hs, 0.0001, 1000);

        State base = LiftCopterStateFactory.createClassicState();
        NavStateMapper map = new NavStateMapper();
        State L1s = map.mapState(base);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, L1s);
        Policy p = vi.planFromState(L1s);
        Episode e = PolicyUtils.rollout(p, env);
        System.out.println(e.actionSequence);
    }

}
