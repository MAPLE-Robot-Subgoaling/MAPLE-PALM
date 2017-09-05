package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BaseDropoffCompletedPF extends PropositionalFunction {

	public BaseDropoffCompletedPF() {
		super("dropoffL1", new String[]{});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;

		return !((boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED));
	}
}
