package taxi.hierarchies.tasks.nav;

import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

public class NavigateActionType extends ObjectParameterizedActionType {

	public NavigateActionType(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction objectParameterizedAction) {
	    return true;
	}
}
