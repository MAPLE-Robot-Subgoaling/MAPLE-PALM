package taxi.functions.rmaxq;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BaseDropoffActionType extends ObjectParameterizedActionType {

	public BaseDropoffActionType(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction a) {
		TaxiState st = (TaxiState) s;

		String[] params = a.getObjectParameters();
		String passengerName = params[0];
		ObjectInstance passenger = st.object(passengerName);

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);

		if(!inTaxi)
			return false;

		for(String locName : st.getLocations()){
			int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
			int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);
			if(tx == lx && ty == ly)
				return true;
		}
		return false;
	}
}
