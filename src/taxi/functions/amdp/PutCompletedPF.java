package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.hierarchies.tasks.put.state.TaxiPutState;
import utilities.MutableObject;

public class PutCompletedPF extends PropositionalFunction{
	//put is complete when the passenger at the put actions goal 
	
	public PutCompletedPF() {
		super("put", new String[]{TaxiPutDomain.CLASS_PASSENGER});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		String passengerName = params[0];
		MutableObject passenger = (MutableObject) s.object(passengerName);
		String passengerGoal = (String) passenger.get(TaxiPutDomain.ATT_GOAL_LOCATION);
		boolean inTaxi = (boolean) passenger.get(TaxiPutDomain.ATT_IN_TAXI);
		String taxiLocation = (String)((TaxiPutState)s).getTaxiAtt(TaxiPutDomain.ATT_TAXI_LOCATION);
		return taxiLocation.equals(passengerGoal) && !inTaxi;
	}
}
