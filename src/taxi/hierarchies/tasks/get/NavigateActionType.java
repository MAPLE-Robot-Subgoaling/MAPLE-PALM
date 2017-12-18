package taxi.hierarchies.tasks.get;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;
import taxi.hierarchies.tasks.get.state.TaxiGetState;

public class NavigateActionType extends ObjectParameterizedActionType {

	public NavigateActionType(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
		TaxiGetState state = (TaxiGetState) s;
		String[] params = objectParameterizedAction.getObjectParameters();
		String locName = params[0];
		ObjectInstance location = state.object(locName);
		return !location.name().equals(state.getTaxiAtt(TaxiGetDomain.ATT_LOCATION));
	}
}
