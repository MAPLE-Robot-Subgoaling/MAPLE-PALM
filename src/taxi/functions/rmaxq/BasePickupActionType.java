package taxi.functions.rmaxq;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.Taxi;
import taxi.state.TaxiState;

public class BasePickupActionType extends ObjectParameterizedActionType {
	public BasePickupActionType(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction a) {
		TaxiState st = (TaxiState) s;

		String[] params = a.getObjectParameters();
		String passengerName = params[0];
		ObjectInstance passenger = st.object(passengerName);

		boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
		int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
		int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		return !inTaxi && tx == px && ty == py;
	}
}
