package taxi.hierGen.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;

import java.util.List;

public class NavActionType implements ActionType {

	@Override
	public String typeName() {
		return TaxiHierGenTask5State.ACTION_Task5_Action;
	}

	@Override
	public Action associatedAction(String strRep) {
		String[] params = strRep.split("_");
		int goalX = Integer.parseInt(params[1]);
		int goalY = Integer.parseInt(params[2]);

		return new HierGenNavAction(goalX, goalY);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		return null;
	}
}
