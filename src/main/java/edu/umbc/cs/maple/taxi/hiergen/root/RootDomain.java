package edu.umbc.cs.maple.taxi.hiergen.root;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.taxi.PutdownActionType;
import edu.umbc.cs.maple.taxi.functions.amdp.PutCompletedPF;
import edu.umbc.cs.maple.taxi.functions.amdp.PutFailurePF;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask5ActionType;
import edu.umbc.cs.maple.taxi.hiergen.actions.HierGenTask7ActionType;
import edu.umbc.cs.maple.taxi.hiergen.task7.state.TaxiHierGenTask7Passenger;
import edu.umbc.cs.maple.taxi.hiergen.task7.state.TaxiHierGenTask7Taxi;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class RootDomain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 taxi domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public RootDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 taxi domain
     */
    public RootDomain() {
//		tf = new NullTermination();
//		rf = new NullRewardFunction();
    }

    public RootDomain(String goalPassengerName) {
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
                new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER}),
                new HierGenTask5ActionType(),
                new HierGenTask7ActionType(ACTION_TASK_7,new String[]{CLASS_PASSENGER})
        );

        return domain;  
    }

    }