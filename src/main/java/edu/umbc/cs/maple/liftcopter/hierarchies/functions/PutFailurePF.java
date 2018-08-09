package edu.umbc.cs.maple.liftcopter.hierarchies.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import edu.umbc.cs.maple.liftcopter.hierarchies.expert.tasks.put.state.LCPutState;
import edu.umbc.cs.maple.utilities.MutableObject;

import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;

public class PutFailurePF extends PropositionalFunction{
    //put fail if taxi is empty

    public PutFailurePF() {
        super("put", new String[]{CLASS_CARGO});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        if (!(s instanceof LCPutState)) { return false; }

        List<ObjectInstance> agents = s.objectsOfClass(CLASS_AGENT);
        if (agents.size() < 1) { return false; }
        ObjectInstance agent = agents.get(0);
        if (agent.get(ATT_LOCATION).equals(ATT_VAL_CRASHED)) { return true; }

        String cargoName = params[0];
        MutableObject cargo = (MutableObject) s.object(cargoName);
        if (cargo == null) { return false; }
        String cargoLocation = (String) cargo.get(ATT_LOCATION);
        return !cargoLocation.equals(ATT_VAL_PICKED_UP);
    }

}
