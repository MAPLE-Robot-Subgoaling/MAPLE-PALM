package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class DropoffCompletedPF extends PropositionalFunction {
	//dropoff is complete when there is no passenger in the taxi
	
	public DropoffCompletedPF() {
		super("dropoffL1", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		
		return !((boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED));
	}

}
