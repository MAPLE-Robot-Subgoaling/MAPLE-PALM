package edu.umbc.cs.maple.liftCopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.get.state.LCGetState;
import edu.umbc.cs.maple.liftCopter.hierarchies.expert.tasks.root.state.LCRootState;
import edu.umbc.cs.maple.liftCopter.state.LiftCopterState;
import edu.umbc.cs.maple.taxi.hierarchies.tasks.root.state.TaxiRootState;

import java.util.List;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;

public class RootFailurePF extends PropositionalFunction {

    public RootFailurePF() {
        super("rootFail", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return (((LCRootState)s).getCopterAtt(ATT_LOCATION) == ATT_VAL_CRASHED);
    }

}
