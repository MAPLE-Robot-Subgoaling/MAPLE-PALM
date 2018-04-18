package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
import utilities.MutableObject;

public class DropoffCompletedPF extends PropositionalFunction {
	//dropoff is complete when there is no passenger in the taxi
	
	public DropoffCompletedPF() {
		super("dropoff", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		if (!(s instanceof TaxiDropoffState)) { return false; }
		MutableObject passenger = (MutableObject)s.object(params[0]); // s.objectsOfClass(Taxi.CLASS_PASSENGER).get(0);//;
		if (passenger == null) {
            if (((TaxiDropoffState)s).numObjects() > 0){
                throw new RuntimeException("ERROR: more than one object, but given param did not match passenger name");
            } else {
                return false;
            }
		}
		String pass_loc = (String)passenger.get(TaxiDropoffDomain.ATT_LOCATION);
		return pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI);
	}

}
