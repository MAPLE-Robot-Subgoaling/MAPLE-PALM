package edu.umbc.cs.maple.liftcopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import edu.umbc.cs.maple.liftcopter.LiftCopter;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.nav.state.LCNavState;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class NavFailurePF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavFailurePF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCNavState)) { return false; }
        return LiftCopter.collidedWithWall(s);
    }
}
