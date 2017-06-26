package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.state.TaxiState;

public class BasePutFailurePF extends PropositionalFunction{
	//put fail when taxi is empty - no abstraction
	
	public BasePutFailurePF() {
		super("put", new String[]{TaxiL1.CLASS_L1LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		
		return !((boolean) st.getTaxiAtt(TaxiL1.ATT_TAXI_OCCUPIED));
	}
	
}
