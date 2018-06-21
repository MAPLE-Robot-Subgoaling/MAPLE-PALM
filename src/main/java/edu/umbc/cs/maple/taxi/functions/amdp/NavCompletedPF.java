package edu.umbc.cs.maple.taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.nav.state.TaxiNavState;

import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class NavCompletedPF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavCompletedPF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof TaxiNavState)) { return false; }
        TaxiNavState st = (TaxiNavState) s;
        List<ObjectInstance> taxis = st.objectsOfClass(CLASS_TAXI);
        if (taxis.size() < 1) {
            return true; // no taxi, we assume it is terminal
        }
        ObjectInstance taxi = taxis.get(0);
        Integer tx = (Integer) taxi.get(ATT_X);
        if (tx == null) { return false; }
        int ty = (int) taxi.get(ATT_Y);
        String locationName = params[0];
        ObjectInstance location = st.object(locationName);
        int lx = (int) location.get(ATT_X);
        int ly = (int) location.get(ATT_Y);
        return tx == lx && ty == ly;
    }

}
