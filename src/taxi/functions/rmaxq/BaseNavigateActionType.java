package taxi.functions.rmaxq;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.ObjectParameterizedActionType;

public class BaseNavigateActionType extends ObjectParameterizedActionType {

	public BaseNavigateActionType(String name, String[] parameterClasses) {
		super(name, parameterClasses);
	}

	@Override
	protected boolean applicableInState(State s, ObjectParameterizedAction a) {
		// We can always nav
	    return true;
	}
}
