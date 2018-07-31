package edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootCargo;
import edu.umbc.cs.maple.liftCopter.hierarchies.functions.RootCompletedPF;
import edu.umbc.cs.maple.liftCopter.hierarchies.functions.RootFailurePF;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;


public class LCRootDomain implements DomainGenerator {

    private RewardFunction rf;
    private TerminalFunction tf;

    /**
     * creates a abstraction 2 taxi domain
     * @param rf rewardTotal function
     * @param tf terminal function
     */
    public LCRootDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
    }

    /**
     * creates a abstraction 2 taxi domain
     */
    public LCRootDomain() {
        this.tf = new GoalFailTF(new RootCompletedPF(), null, new RootFailurePF(), null);
        this.rf = new GoalFailRF((GoalFailTF) tf);
    }


    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_CARGO, LCRootCargo.class);

        LCRootModel tmodel = new LCRootModel();
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
                new GetActionType(ACTION_GET, new String[]{CLASS_CARGO}),
                new PutActionType(ACTION_PUT, new String[]{CLASS_CARGO})
        );

        return domain;
    }

}
