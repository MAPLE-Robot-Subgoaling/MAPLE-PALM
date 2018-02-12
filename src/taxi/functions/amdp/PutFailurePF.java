package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import utilities.MutableObject;

import static taxi.hierarchies.tasks.root.TaxiRootDomain.ATT_GOAL_LOCATION;

public class PutFailurePF extends PropositionalFunction{
	//put fail if taxi is empty 
	
	public PutFailurePF() {
		super("put", new String[]{Taxi.CLASS_PASSENGER});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String passengerName = params[0];
		MutableObject passenger = (MutableObject) s.object(passengerName);
		if (passenger == null) { return false; }
		String passengerLocation = (String) passenger.get(TaxiPutDomain.ATT_LOCATION);
		return !passengerLocation.equals(TaxiPutDomain.IN_TAXI);
	}
	
}
