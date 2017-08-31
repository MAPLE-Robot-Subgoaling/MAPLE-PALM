package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import org.apache.commons.lang3.mutable.Mutable;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.root.GetActionType;
import taxi.hierarchies.tasks.get.state.TaxiGetState;
import utilities.MutableObject;

public class GetCompletedPF extends PropositionalFunction{ 
	// get is complete when desired passenger is in the taxi
	
	public GetCompletedPF() {
		super("get", new String[]{TaxiGetDomain.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String passengerName = params[0];
		MutableObject passenger = (MutableObject) s.object(passengerName);
		String pass_loc = (String) passenger.get(TaxiGetDomain.ATT_LOCATION);
		return pass_loc.equals(TaxiGetDomain.IN_TAXI);
	}
}
