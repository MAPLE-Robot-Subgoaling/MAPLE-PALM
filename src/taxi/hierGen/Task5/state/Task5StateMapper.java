package taxi.hierGen.Task5.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import static taxi.TaxiConstants.ATT_X;
import static taxi.TaxiConstants.ATT_Y;

public class Task5StateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(ATT_X);
		int ty = (int) st.getTaxiAtt(ATT_Y);
		TaxiHierGenTask5Taxi taxi = new TaxiHierGenTask5Taxi(st.getTaxiName(), tx, ty);

		return new TaxiHierGenTask5State(taxi);
	}
}
