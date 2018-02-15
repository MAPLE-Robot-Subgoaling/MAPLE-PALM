package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.bringon.TaxiBringonDomain;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;
import utilities.MutableObject;

public class BringonCompletedPF extends PropositionalFunction {
	//pickup is complete when passenger is in taxi 
	
	public BringonCompletedPF() {
		super("bringon", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		if (!(s instanceof TaxiBringonState)) { return false; }
		MutableObject passenger = (MutableObject) s.object(params[0]);
		if (passenger == null) { return false; }
		String pass_loc = (String)passenger.get(TaxiBringonDomain.ATT_LOCATION);
		return pass_loc.equals(TaxiBringonDomain.IN_TAXI);
	}

}
