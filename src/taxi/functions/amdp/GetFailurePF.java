package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;

public class GetFailurePF extends PropositionalFunction {
	//get fails if any passenger if in taxi unless it is the right one
	
	public GetFailurePF() {
		super("getFail", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
//		String passengerName = params[0];
//		MutableObject passenger = (MutableObject) s.object(passengerName);
//		String pass_loc = (String) passenger.get(TaxiGetDomain.ATT_LOCATION);
//		String taxi_loc = (String) ((TaxiGetState)s).getTaxiAtt(TaxiGetDomain.ATT_LOCATION);
		return false;
	}

}
