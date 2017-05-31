package taxi.amdp.level1;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import taxi.amdp.level1.state.TaxiL1Location;
import taxi.amdp.level1.state.TaxiL1Passenger;
import taxi.amdp.level1.state.TaxiL1State;

import java.util.List;

/**
 * Created by ngopalan on 8/13/16.
 */
public class TaxiL1TerminalFunction implements TerminalFunction {


    @Override
    public boolean isTerminal(State state) {
        List<TaxiL1Passenger> passengerList = ((TaxiL1State)state).passengers;
        List<TaxiL1Location> locationList = ((TaxiL1State)state).locations;

        for(TaxiL1Passenger p:passengerList){
            if(p.inTaxi){
                return false;
            }
            String goalLocation = p.goalLocation;
            for(TaxiL1Location l :locationList){
                if(goalLocation.equals(l.colour)){
                    if(p.currentLocation.equals(l.colour) && p.pickUpOnce){
                        break;
                    }
                    else{
                        return false;
                    }
                }
            }
        }
        return true;
    }


}
