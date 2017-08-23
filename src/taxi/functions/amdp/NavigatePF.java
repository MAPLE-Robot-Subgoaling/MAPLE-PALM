package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.hierarchies.tasks.nav.NavigateActionType;
import taxi.hierarchies.tasks.nav.NavigateActionType.NavigateAction;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

public class NavigatePF extends PropositionalFunction {
	//nav is terminal when the taxi is at the desired location
	
	public NavigatePF() {
		super("Nav to depot", new String[]{TaxiNavDomain.CLASS_LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		String action = params[0];
		TaxiNavState st = (TaxiNavState) s;
		NavigateActionType nav = new NavigateActionType();
		NavigateAction a = nav.associatedAction(action);
		String goal = (String)a.getGoalLocation();

		int tx = (int) st.getTaxiAtt(TaxiNavDomain.ATT_X);
		int ty = (int) st.getTaxiAtt(TaxiNavDomain.ATT_Y);
		int lx = (int) st.getLocationAtt(goal, TaxiNavDomain.ATT_X);
		int ly = (int) st.getLocationAtt(goal, TaxiNavDomain.ATT_Y);

		return tx == lx && ty == ly;
	}

}
