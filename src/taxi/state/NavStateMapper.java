package taxi.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

public class NavStateMapper implements StateMapping {

	@Override
	public State mapState(State s) {
		TaxiState st = (TaxiState) s;
		TaxiState snav = st.copy();
		snav.makeNaveState();
		
		TaxiAgent taxi = snav.touchTaxi();
		taxi.abstractNavigate();
		
		for(String locName : st.getLocations()){
			TaxiLocation l = snav.touchLocation(locName);
			l.abstractNavigate();
		}
		
		return snav;
	}

}
