package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.state.NavStateMapper;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

public class NavCompletedPF extends PropositionalFunction {
	//nav is terminal when the taxi is at the desired location

	public NavCompletedPF() {
		super("Nav to depot", new String[]{Taxi.CLASS_LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiNavState st = new NavStateMapper().mapState(s);
		if (st.objectsOfClass(Taxi.CLASS_TAXI).size() < 1) { return false; }
		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);
		int lx = (int) st.getLocationAtt(params[0], Taxi.ATT_X);
		int ly = (int) st.getLocationAtt(params[0], Taxi.ATT_Y);
		return tx == lx && ty == ly;
	}

}
