package taxi.functions.rmaxq;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.state.TaxiState;
import taxi.functions.rmaxq.BaseNavigateActionType.NavigeteAction;

public class BaseNavigatePF extends PropositionalFunction {
	public BaseNavigatePF() {
		super("base Nav", new String[]{Taxi.CLASS_LOCATION});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiState st = (TaxiState) s;
		BaseNavigateActionType anav = new BaseNavigateActionType();
		NavigeteAction action = anav.associatedAction(params[0]);
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		int lx = (int) st.getLocationAtt(action.getGoalLocation(), Taxi.ATT_X);
		int ly = (int) st.getLocationAtt(action.getGoalLocation(), Taxi.ATT_Y);
		return tx == lx && ty == ly;
	}
}
