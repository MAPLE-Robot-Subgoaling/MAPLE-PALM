package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.hierarchies.tasks.get.state.TaxiGetState;
import utilities.MutableObject;

public class GetCompletedPF extends PropositionalFunction{ 
	// get is complete when desired passenger is in the taxi
	
	public GetCompletedPF() {
		super("get", new String[]{Taxi.CLASS_PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		if (!(s instanceof TaxiGetState)) { return false; }
		String passengerName = params[0];
		MutableObject passenger = (MutableObject) s.object(passengerName);
		if (passenger == null) { return false; }
		String pass_loc = (String) passenger.get(TaxiGetDomain.ATT_LOCATION);
		return pass_loc.equals(TaxiGetDomain.IN_TAXI);
	}
}
