package cleanup.hierarchies.tasks.pick;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

import static cleanup.Cleanup.ATT_REGION;

public class PickObjectRoomGoalPF extends PropositionalFunction {

    public PickObjectRoomGoalPF(String name, String[] parameterClasses) {
        super(name, parameterClasses);
    }

    @Override
    public boolean isTrue(OOState state, String[] params) {
        String objectName = params[0];
        String regionName = params[1];
        ObjectInstance object = state.object(objectName);
        ObjectInstance region = state.object(regionName);
        // if the region is false, most likely because the object is not in any region
        if (region == null) { return false; }
        return object.get(ATT_REGION).equals(region.name());
    }
}