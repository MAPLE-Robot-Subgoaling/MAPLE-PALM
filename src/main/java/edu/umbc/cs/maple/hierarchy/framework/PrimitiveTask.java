package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;

public class PrimitiveTask extends Task{

    /**
     * creates a base level task
     * @param aType the action type in the base domain
     * @param abstractDomain the base domain
     */
    public PrimitiveTask(ActionType aType, OOSADomain abstractDomain) {
        super(null, aType, abstractDomain, new IdentityMap());
    }

    @Override
    public double reward(GroundedTask parent, State groundState, State abstractState, Action a, State groundStatePrime, State abstractStatePrime, String[] params) {
        return ((FactoredModel)this.domain.getModel()).getRf().reward(abstractState, a, abstractStatePrime);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isFailure(State s, String[] params, boolean unsetParams) {
        return false;
    }
    @Override
    public boolean isComplete(State s, String[] params, boolean unsetParams){
        return true;
    }


}
