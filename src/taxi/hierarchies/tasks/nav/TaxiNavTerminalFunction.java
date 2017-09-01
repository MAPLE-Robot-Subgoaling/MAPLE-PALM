package taxi.hierarchies.tasks.nav;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

public class TaxiNavTerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiNavState st = (TaxiNavState)s;

	    for(String loc : st.getLocations()) {
	    	if(loc.equals(st.getTaxiAtt(Taxi.CLASS_LOCATION))) {
	    		return true;
			}
		}

		return false;
	}

}
