package taxi.hierGen.Task5.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

public class Task5StateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_X);
		TaxiHierGenTask5Taxi taxi = new TaxiHierGenTask5Taxi(st.getTaxiName(), tx, ty);

		return new TaxiHierGenTask5State(taxi);
	}
}
