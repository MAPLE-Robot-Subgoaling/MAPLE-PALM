package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import static taxi.TaxiConstants.*;

public class TaxiTerminalFunction implements TerminalFunction{
    //the taxi domain is terminal when all passengers are at their goal
    //and have been picked up and not in the taxi anymore

    @Override
    public boolean isTerminal(State s) {
        TaxiState state = (TaxiState) s;

        for(String passengerName : state.getPassengers()){
            boolean inTaxi = (boolean) state.getPassengerAtt(passengerName, ATT_IN_TAXI);
            if(inTaxi)
                return false;

            String passengerGoal = (String) state.getPassengerAtt(passengerName, ATT_GOAL_LOCATION);
            int px = (int) state.getPassengerAtt(passengerName, ATT_X);
            int py = (int) state.getPassengerAtt(passengerName, ATT_Y);

            for(String locName : state.getLocations()){
                if(passengerGoal.equals(locName)){
                    int lx = (int) state.getLocationAtt(locName, ATT_X);
                    int ly = (int) state.getLocationAtt(locName, ATT_Y);
                    if(lx != px || ly != py)
                        return false;

                    break;
                }
            }
        }

        return true;
    }
}
