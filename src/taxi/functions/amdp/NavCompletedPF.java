package taxi.functions.amdp;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.hierarchies.tasks.nav.state.TaxiNavState;

import static taxi.TaxiConstants.*;

public class NavCompletedPF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavCompletedPF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof TaxiNavState)) { return false; }
        TaxiNavState st = (TaxiNavState) s;
        ObjectInstance taxi = st.objectsOfClass(CLASS_TAXI).get(0);
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
