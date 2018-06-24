package edu.umbc.cs.maple.taxi.hiergen.task7;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
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
import edu.umbc.cs.maple.taxi.PickupActionType;
import edu.umbc.cs.maple.taxi.functions.amdp.PutCompletedPF;
import edu.umbc.cs.maple.taxi.functions.amdp.PutFailurePF;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.NavigateActionType;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.PutStateMapper;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutAgent;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutLocation;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.put.state.TaxiPutPassenger;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5ActionType;
import edu.umbc.cs.maple.taxi.hiergen.root.state.TaxiHierGenRootPassenger;
import edu.umbc.cs.maple.taxi.hiergen.task7.state.TaxiHierGenTask7Passenger;
import edu.umbc.cs.maple.taxi.hiergen.task7.state.TaxiHierGenTask7Taxi;
import edu.umbc.cs.maple.taxi.stategenerator.TaxiStateFactory;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class Task7Domain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 taxi domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public Task7Domain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 taxi domain
     */
    public Task7Domain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public Task7Domain(String goalPassengerName) {
        String[] params = new String[]{goalPassengerName};
        this.tf = new GoalFailTF(new PutCompletedPF(), params, new PutFailurePF(), params);
        this.rf = new GoalFailRF((GoalFailTF) tf);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_PASSENGER, TaxiHierGenTask7Passenger.class)
                .addStateClass(CLASS_TAXI, TaxiHierGenTask7Taxi.class);

        if (tf == null) {
            System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null TF");
            tf = new NullTermination();
        }
        if (rf == null) {
            System.err.println("Warning: initializing " + this.getClass().getSimpleName() + " with Null RF");
            rf = new NullRewardFunction();
        }

        domain.addActionTypes(
                new PickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER}),
                new HierGenTask5ActionType()
        );

        return domain;
    }

    }