package taxi.hiergen;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;

public abstract class TaxiHierGenState implements MutableOOState {

    public abstract ObjectInstance getTaxi();

}
