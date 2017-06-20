package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.abstraction1.TaxiL1;
import taxi.state.TaxiState;

public class BaseGetTerminalPF extends PropositionalFunction {

	public BaseGetTerminalPF() {
		super("get", new String[]{TaxiL1.CLASS_L1PASSENGER});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		
		boolean ret = (boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);
		return ret;
	}

}
