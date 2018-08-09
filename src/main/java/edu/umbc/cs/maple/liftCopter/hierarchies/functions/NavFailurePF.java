package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.LiftCopter;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.nav.state.LCNavState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.put.state.LCPutState;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterState;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class NavFailurePF extends PropositionalFunction {
    //nav is terminal when the taxi is at the desired location

    public NavFailurePF() {
        super("Nav to depot", new String[]{CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCNavState)) { return false; }
        if (LiftCopter.collidedWithWall(s)) {
            return true;
        }
        return false;
    }
}
