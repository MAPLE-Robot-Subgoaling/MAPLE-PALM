package edu.umbc.cs.maple.taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.TaxiNavState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class NavCompletedPF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavCompletedPF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
//		TaxiNavState st = new NavStateMapper().mapState(s);
        if (!(s instanceof TaxiNavState)) { return false; }
        TaxiNavState st = (TaxiNavState) s;
        Integer tx = (Integer) st.getTaxiAtt(ATT_X);
        if (tx == null) { return false; }
        int ty = (int) st.getTaxiAtt(ATT_Y);
        int lx = (int) st.getLocationAtt(params[0], ATT_X);
        int ly = (int) st.getLocationAtt(params[0], ATT_Y);
        return tx == lx && ty == ly;
    }

}
