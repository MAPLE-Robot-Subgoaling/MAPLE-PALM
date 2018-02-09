package taxi.hierarchies;

import burlap.mdp.core.oo.state.MutableOOState;

public abstract class TaxiGetPutState implements MutableOOState{

    public abstract Object getTaxiAtt(String attName);

}
