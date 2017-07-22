package testing;

import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.TaxiDBNParents;
import taxi.stateGenerator.TaxiStateFactory;

public class CATTest {

	public static void main(String[] args) {
		
		State init = TaxiStateFactory.createSmallState();
		Taxi domain = new Taxi();
		
		TaxiDBNParents.getParents(init);

	}

}
