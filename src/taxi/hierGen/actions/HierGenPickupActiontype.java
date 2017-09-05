package taxi.hierGen.actions;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.Taxi;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;

public class HierGenPickupActiontype extends ObjectParameterizedActionType {
	public HierGenPickupActiontype(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction a) {
		String[] params = a.getObjectParameters();
		String passengerName = params[0];
		TaxiHierGenTask7State st = (TaxiHierGenTask7State) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
		int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
		boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);

		return !inTaxi && tx == px && ty == py;
	}
}
