package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction1.state.TaxiL1State;

public class PutTerminalPF extends PropositionalFunction{

	public PutTerminalPF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiL1State st = (TaxiL1State) s;
		
		return !((boolean) st.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED));
	}
	
}
