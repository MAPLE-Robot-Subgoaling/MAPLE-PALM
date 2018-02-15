package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class BringonFailurePF extends PropositionalFunction {
	//pickup fails when taxi is not at a depot
	public BringonFailurePF() {
		super("bringonFail", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		if (!(s instanceof TaxiBringonState)) { return false; }
		TaxiBringonState st = (TaxiBringonState)s;
		if (st.numObjects() < 1) { return false; }
		String passengerName = st.objectsOfClass(Taxi.CLASS_PASSENGER).get(0).name();
		String pass_loc = (String)st.getPassengerAtt(passengerName, TaxiBringonDomain.ATT_LOCATION);
		String taxi_loc = (String)st.getTaxiAtt(TaxiBringonDomain.ATT_LOCATION);
		return !pass_loc.equals(taxi_loc);
	}

}
