package taxi.hierGen.root.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class HierGenRootStateMapper implements StateMapping {
	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_X);
		TaxiHierGenRootTaxi taxi = new TaxiHierGenRootTaxi(st.getTaxiName(), tx, ty);

		List<TaxiHierGenRootPassenger> passengers = new ArrayList<TaxiHierGenRootPassenger>();
		for(String pnam : st.getPassengers()){
			int px = (int) st.getPassengerAtt(pnam, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(pnam, Taxi.ATT_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(pnam, Taxi.ATT_IN_TAXI);

			String goalLoc = (String) st.getPassengerAtt(pnam, Taxi.ATT_GOAL_LOCATION);
			int destX = (int) st.getLocationAtt(goalLoc, Taxi.ATT_X);
			int destY = (int) st.getLocationAtt(goalLoc, Taxi.ATT_Y);


			passengers.add(new TaxiHierGenRootPassenger(pnam, px, py, destX, destY, inTaxi));
		}

		return new TaxiHierGenRootState(taxi, passengers);
	}
}
