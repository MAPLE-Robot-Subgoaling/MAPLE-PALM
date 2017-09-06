package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;
import taxi.hierarchies.interfaces.PassengerLocationParameterizable;

import java.util.ArrayList;
import java.util.List;

public class HierGenTask5ActionType implements ActionType {

	@Override
	public String typeName() {
		return TaxiHierGenTask5State.ACTION_Task5_Action;
	}

	@Override
	public Action associatedAction(String strRep) {
		String[] params = strRep.split("_");
		int goalX = Integer.parseInt(params[1]);
		int goalY = Integer.parseInt(params[2]);

		return new HierGenTask5Action(goalX, goalY);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		List<Action> actions = new ArrayList<Action>();
		PassengerLocationParameterizable st = (PassengerLocationParameterizable) s;

		for(String pname : st.getPassengers()){
			int goalX = st.getLocationX(pname);
			int goalY = st.getLocationY(pname);
			actions.add(new HierGenTask5Action(goalX, goalY));
		}
		return actions;
	}
}
