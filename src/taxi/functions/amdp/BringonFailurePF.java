package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.get.BringonActionType;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class BringonFailurePF extends PropositionalFunction {
	//pickup fails when taxi is not at a depot
	public BringonFailurePF() {
		super("bringonFail", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiBringonState st = (TaxiBringonState)s;
		BringonActionType pickup = new BringonActionType();
		BringonActionType.BringonAction a = pickup.associatedAction(action);
		String passenger = a.getPassenger();
		String pass_loc = (String)st.getPassengerAtt(passenger, TaxiBringonDomain.ATT_LOCATION);
		String taxi_loc = (String)st.getTaxiAtt(TaxiBringonDomain.ATT_LOCATION);

		return !pass_loc.equals(taxi_loc);
	}

}
