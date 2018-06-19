package edu.umbc.cs.maple.taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.taxi.state.TaxiState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class TaxiTerminalFunction implements TerminalFunction{
    //the taxi domain is terminal when all passengers are at their goal
    //and have been picked up and not in the taxi anymore

    @Override
    public boolean isTerminal(State s) {
        TaxiState state = (TaxiState) s;

        for(ObjectInstance passenger : state.objectsOfClass(CLASS_PASSENGER)){
            int px = (int) passenger.get(ATT_X);
            int py = (int) passenger.get(ATT_Y);
            boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
            if(inTaxi)
                return false;

            String passengerGoal = (String) passenger.get(ATT_GOAL_LOCATION);

            for (ObjectInstance location : state.objectsOfClass(CLASS_LOCATION)) {
                int lx = (int) location.get(ATT_X);
                int ly = (int) location.get(ATT_Y);
                if(passengerGoal.equals(location.name())){
                    if(lx != px || ly != py)
                        return false;

                    break;
                }
            }
        }

        return true;
    }
}
