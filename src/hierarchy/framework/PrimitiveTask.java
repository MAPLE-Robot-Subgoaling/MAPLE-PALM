package hierarchy.framework;

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

    //primitive actions are assumed to always complete
    @Override
    public boolean isFailure(State s, Action a) {
        return false;
    }

    @Override
    public boolean isComplete(State s, Action a){
        return true;
    }

    @Override
    public double reward(State s, Action a, State sPrime) {
        return ((FactoredModel)this.domain.getModel()).getRf().reward(s, a, sPrime);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }
}
