package taxi.amdp.functions;

import java.util.List;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.abstraction2.TaxiL2;
import taxi.abstraction2.state.TaxiL2State;

public class RootPF extends PropositionalFunction {

	public RootPF() {
		super("root", new String[]{TaxiL2.CLASS_L2LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiL2State st = (TaxiL2State) s;
		
		for(String passengerName : st.getPassengers()){
			boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, TaxiL2.ATT_IN_TAXI);
			boolean pickedUp = (boolean) st.getPassengerAtt(passengerName, TaxiL2.ATT_PICKED_UP_AT_LEAST_ONCE);
			String locationName = (String) st.getPassengerAtt(passengerName, TaxiL2.ATT_CURRENT_LOCATION);
			String goalLocationColor = (String) st.getPassengerAtt(passengerName, TaxiL2.ATT_GOAL_LOCATION);
		
			if(inTaxi || !pickedUp)
				return false;
			
			boolean rightLocation=false;
			for(String color : (List<String>)st.getLocationAtt(locationName, Taxi.ATT_COLOR))
				if (color.equals(goalLocationColor))
					rightLocation=true;
			if(!rightLocation)
				return false;
		}
 		return true;
	}

}
