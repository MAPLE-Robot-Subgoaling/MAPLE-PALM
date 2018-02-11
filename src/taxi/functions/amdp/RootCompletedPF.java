package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;

import taxi.Taxi;
import taxi.hierarchies.tasks.root.TaxiRootDomain;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.root.state.TaxiRootState;


public class RootCompletedPF extends PropositionalFunction {

	public RootCompletedPF() {
		super("root", new String[]{});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiRootState st = (TaxiRootState) s;

		for(String passengerName : st.getPassengers()){
			String locationName = (String) st.getPassengerAtt(passengerName, TaxiRootDomain.ATT_CURRENT_LOCATION);
			String goalLocation = (String) st.getPassengerAtt(passengerName, TaxiRootDomain.ATT_GOAL_LOCATION);
		
			if(!locationName.equals(goalLocation))
				return false;
		}
 		return true;
	}

}
