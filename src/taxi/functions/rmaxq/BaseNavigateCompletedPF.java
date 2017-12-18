package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BaseNavigateCompletedPF extends PropositionalFunction {
	public BaseNavigateCompletedPF() {
		super("base Nav", new String[]{Taxi.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		int lx = (int) st.getLocationAtt(params[0], Taxi.ATT_X);
		int ly = (int) st.getLocationAtt(params[0], Taxi.ATT_Y);
		return tx == lx && ty == ly;
	}
}
