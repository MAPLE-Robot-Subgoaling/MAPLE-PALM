package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.put.TaxiPutDomain;
import taxi.state.TaxiState;

public class BasePutFailurePF extends PropositionalFunction{
	//put fail when taxi is empty - no abstraction
	
	public BasePutFailurePF() {
		super("put", new String[]{TaxiPutDomain.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;

		boolean occupied = false;
		for(String p : st.getPassengers()) {
			if((boolean)st.getPassengerAtt(p, TaxiPutDomain.ATT_IN_TAXI)) {
				return false;
			}
		}

		return true;
	}
	
}
