package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import utilities.MutableObject;

public class GetFailurePF extends PropositionalFunction {
	//get fails if any passenger if in taxi unless it is the right one
	
	public GetFailurePF() {
		super("getFail", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
	    return false;
	}

}
