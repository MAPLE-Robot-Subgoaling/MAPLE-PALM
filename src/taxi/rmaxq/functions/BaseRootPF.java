package taxi.rmaxq.functions;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import taxi.state.TaxiState;

import static taxi.TaxiConstants.*;

public class BaseRootPF  extends PropositionalFunction{
    //root is complet when all pasengers are at their goal and not in taxi

    public BaseRootPF (){
        super("root", new String[]{});
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        TaxiState state = (TaxiState) s;

        for(ObjectInstance passenger : state.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            if (inTaxi )
                return false;

            String passengerGoal = (String) passenger.get(ATT_GOAL_LOCATION);

            for(ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)){
                if (passengerGoal.equals(location.name())) {
                    int lx = (int) location.get(ATT_X);
                    int ly = (int) location.get(ATT_Y);
                    if (lx != px || ly != py)
                        return false;

                    break;
                }
            }
        }
        return true;
    }
}
