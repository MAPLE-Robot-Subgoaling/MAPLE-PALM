package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierarchies.tasks.nav.TaxiNavDomain;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

public class NavigateAbstractPF extends PropositionalFunction {
	//nav is terminal when the taxi is at the desired location
	
	public NavigateAbstractPF() {
		super("Nav to depot", new String[]{Taxi.CLASS_LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
		TaxiNavState st = (TaxiNavState) s;
		int tx = (int) st.getTaxiAtt(TaxiNavDomain.ATT_X);
		int ty = (int) st.getTaxiAtt(TaxiNavDomain.ATT_Y);
		int lx = (int) st.getLocationAtt(params[0], TaxiNavDomain.ATT_X);
		int ly = (int) st.getLocationAtt(params[0], TaxiNavDomain.ATT_Y);
		return tx == lx && ty == ly;
	}

}
