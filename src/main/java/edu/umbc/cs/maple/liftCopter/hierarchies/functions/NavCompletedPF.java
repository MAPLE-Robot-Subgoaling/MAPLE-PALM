package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
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
        Integer tx = (Integer) st.getAgentAtt(ATT_X);
        if (tx == null) { return false; }
        int ty = (int) st.getAgentAtt(ATT_Y);
        int lx = (int) st.getLocationAtt(params[0], ATT_X);
        int ly = (int) st.getLocationAtt(params[0], ATT_Y);
        return tx == lx && ty == ly;
    }

}
