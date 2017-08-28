package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierGen.Task7.state.TaxiHierGenTask7State;

import java.util.ArrayList;
import java.util.List;

public abstract class Task7Task5ActionType implements ActionType {
	@Override
	public List<Action> allApplicableActions(State s) {
		TaxiHierGenTask7State st = (TaxiHierGenTask7State) s;
		List<Action> actions = new ArrayList<Action>();
		for(String pname : st.getPassengers()){
			int px = (int) st.getPassengerAtt(pname, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(pname, Taxi.ATT_Y);
			actions.add(new HierGenNavAction(px, py));
		}
		return actions;
	}
}
