package edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.NavigateActionType;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.LCGetAgent;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.LCGetCargo;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.get.state.LCGetLocation;
import edu.umbc.cs.maple.liftcopter.hierarchies.functions.GetCompletedPF;
import edu.umbc.cs.maple.liftcopter.hierarchies.functions.GetFailurePF;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

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

        domain.addStateClass(CLASS_CARGO, LCGetCargo.class)
            .addStateClass(CLASS_AGENT, LCGetAgent.class)
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
                new GetPickupActionType(ACTION_PICKUP, new String[]{CLASS_CARGO})
        );

        return domain;
    }



}
