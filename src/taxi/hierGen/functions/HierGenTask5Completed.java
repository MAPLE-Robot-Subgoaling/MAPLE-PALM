package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierGen.Task5.state.TaxiHierGenTask5State;
import taxi.hierGen.actions.HierGenNavAction;
import taxi.hierGen.actions.NavActionType;

public class HierGenTask5Completed extends PropositionalFunction {

	public HierGenTask5Completed(){
		super("task5", new String[]{});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		//tx == goalx ty \\goaly

		TaxiHierGenTask5State st = (TaxiHierGenTask5State) s;
		NavActionType navType = new NavActionType();
		HierGenNavAction action = (HierGenNavAction) navType.associatedAction(params[0]);

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		int goalX = action.getGoalX();
		int goalY = action.getGoalY();

		return tx == goalX && ty == goalY;
	}
}
