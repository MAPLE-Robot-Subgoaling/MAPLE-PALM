package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetLocation;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavState;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class NavCompletedPF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavCompletedPF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
//		LCNavState st = new NavStateMapper().mapState(s);
        if (!(s instanceof LCNavState)) { return false; }
        LCNavState st = (LCNavState) s;
        Double ax = (Double)st.touchCopter().get(ATT_X);
        if (ax == null) { return false; }
        double ay = (double)st.touchCopter().get(ATT_Y);
        double ah = (double)st.touchCopter().get(ATT_H);
        double aw = (double)st.touchCopter().get(ATT_W);
        double lx = (double) st.getLocationAtt(params[0], ATT_X);
        double ly = (double) st.getLocationAtt(params[0],ATT_Y);
        double lh = (double) st.getLocationAtt(params[0],ATT_H);
        double lw = (double) st.getLocationAtt(params[0],ATT_W);
        return (lx < ax + aw &&
                lx + lw > ax &&
                ly < ay + ah &&
                ly + lh > ay) ;


    }

}
