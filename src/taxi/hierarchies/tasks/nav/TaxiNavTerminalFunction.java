package taxi.hierarchies.tasks.nav;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

public class TaxiNavTerminalFunction implements TerminalFunction {
	//the taxi domain is terminal when all passengers are at their goal
	//and have been picked up and not in the taxi anymore
	
	@Override
	public boolean isTerminal(State s) {
		TaxiNavState st = (TaxiNavState)s;
		int tx = (int)st.getTaxiAtt(TaxiNavDomain.ATT_X);
		int ty = (int)st.getTaxiAtt(TaxiNavDomain.ATT_Y);

	    for(String loc : st.getLocations()) {
	    	int lx = (int)st.getLocationAtt(loc, TaxiNavDomain.ATT_X);
			int ly = (int)st.getLocationAtt(loc, TaxiNavDomain.ATT_Y);
	    	if(lx == tx && ly == ty) {
	    		return true;
			}
		}

		return false;
	}

}
