package taxi.amdp.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;

public class DropoffFailurePF extends PropositionalFunction {
	//dropoff fails if taxi is not at a depot  
	
	public DropoffFailurePF() {
		super("pickup>L!", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		// if the taxi is not at depot or not occupied
		TaxiState st = (TaxiState) s;
		
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		
		for(String locationName : st.getLocations()){
			int lx = (int) st.getLocationAtt(locationName, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(locationName, Taxi.ATT_Y);
			if(tx == lx && ty == ly){
				int passCount = 0;
				for(String passengerName : st.getPassengers()){
					int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
					int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
					if(px == lx && py == ly)
						passCount++;
				}
				return !((boolean) st.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED)) || passCount > 1;
			}
		}
		
		return true;
	}
}
