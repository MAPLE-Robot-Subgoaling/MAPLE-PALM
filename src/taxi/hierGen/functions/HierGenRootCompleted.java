package taxi.hierGen.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import taxi.Taxi;
import taxi.hierGen.root.state.TaxiHierGenRootState;

public class HierGenRootCompleted extends PropositionalFunction {

	public HierGenRootCompleted(){
		super("root", new String[]{});
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		//intaxi = false, t.x == p.x t.y ==p.y, t.x == p.destx, t.y ==p.desty

		TaxiHierGenRootState st = (TaxiHierGenRootState) s;

		int tx = (int) st.getTaxiAtt(Taxi.ATT_X);
		int ty = (int) st.getTaxiAtt(Taxi.ATT_Y);

		for(String pname : st.getPassengers()){
			int px = (int) st.getPassengerAtt(pname, Taxi.ATT_X);
			int py = (int) st.getPassengerAtt(pname, Taxi.ATT_Y);
			int destX = (int) st.getPassengerAtt(pname, TaxiHierGenRootState.ATT_DESTINAION_X);
			int destY = (int) st.getPassengerAtt(pname, TaxiHierGenRootState.ATT_DESTINAION_Y);
			boolean inTaxi = (boolean) st.getPassengerAtt(pname, Taxi.ATT_IN_TAXI);

			if(inTaxi)
				return false;

			if(tx != px || ty != py)
				return false;

			if(tx != destX || ty != destY)
				return false;
		}
		return true;

	}
}
