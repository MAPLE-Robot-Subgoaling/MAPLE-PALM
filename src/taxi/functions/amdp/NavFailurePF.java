package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

import static taxi.TaxiConstants.CLASS_LOCATION;

public class NavFailurePF extends PropositionalFunction {
	//nav is terminal when the taxi is at the desired location

	public NavFailurePF() {
		super("Nav to depot", new String[]{CLASS_LOCATION});
	}
	
	@Override
	public boolean isTrue(OOState s, String... params) {
	    return false;
	}

}
