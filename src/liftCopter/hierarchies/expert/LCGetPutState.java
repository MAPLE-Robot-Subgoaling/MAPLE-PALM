package liftCopter.hierarchies.expert;

import burlap.mdp.core.oo.state.MutableOOState;

public abstract class LCGetPutState implements MutableOOState{

    public abstract Object getAgentAtt(String attName);

}
