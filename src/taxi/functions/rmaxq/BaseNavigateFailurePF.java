package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BaseNavigateFailurePF extends PropositionalFunction {
	public BaseNavigateFailurePF() {
		super("base Nav", new String[]{Taxi.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);

		for(String loc : st.getLocations()) {
			int lx = (int) st.getLocationAtt(loc, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(loc, Taxi.ATT_Y);
			if(lx == tx && ly == ty) {
				return true;
			}
		}
		return false;
	}
}
