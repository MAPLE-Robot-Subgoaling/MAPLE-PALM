package taxi.amdp.functions;

import java.util.List;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.abstraction1.TaxiL1;
import taxi.abstraction2.TaxiL2;
import taxi.abstraction2.state.TaxiL2State;

public class RootPF extends PropositionalFunction {

	public RootPF() {
		super("root", new String[]{TaxiL1.CLASS_L1LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiL2State st = (TaxiL2State) s;
		
		for(String pass : st.getPassengers()){
			String currentLocation = (String) st.getPassengerAtt(pass, TaxiL2.ATT_CURRENT_LOCATION);
			String goalLocation = (String) st.getPassengerAtt(pass, TaxiL2.ATT_GOAL_LOCATION);
			
			List<String> colors = (List<String>) st.getLocationAtt(currentLocation, TaxiL2.ATT_COLOR); 
			
			if(!colors.contains(goalLocation))
				return false;
		}
		return true;
	}

}
