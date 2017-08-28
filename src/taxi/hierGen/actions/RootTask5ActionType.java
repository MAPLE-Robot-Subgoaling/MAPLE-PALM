package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import taxi.hierGen.root.state.TaxiHierGenRootState;

import java.util.ArrayList;
import java.util.List;

public class RootTask5ActionType extends NavActionType {
	@Override
	public List<Action> allApplicableActions(State s) {
		// nav to pass destination
		TaxiHierGenRootState st = (TaxiHierGenRootState) s;
		List<Action> actions = new ArrayList<Action>();
		for(String pname : st.getPassengers()){
			int destX = (int) st.getPassengerAtt(pname, TaxiHierGenRootState.ATT_DESTINAION_X);
			int destY = (int) st.getPassengerAtt(pname, TaxiHierGenRootState.ATT_DESTINAION_Y);
			actions.add(new HierGenNavAction(destX, destY));
		}
		return actions;
	}
}
