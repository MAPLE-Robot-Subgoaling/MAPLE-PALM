package taxi.amdp.level2.state;

import static taxi.amdp.level1.TaxiL1Domain.ON_ROAD;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import taxi.amdp.level1.state.TaxiL1Passenger;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;


/**
 * Created by ngopalan on 8/12/16.
 */
public class L2StateMapper implements StateMapping{
    @Override
    public State mapState(State s) {
        TaxiState sL0 = (TaxiState)s;
        List<TaxiL2Location> locationsL1 = new ArrayList<TaxiL2Location>();

        for(TaxiLocation l0 : sL0.locations){
            TaxiL2Location l1 = new TaxiL2Location(l0.name() ,l0.colour);
            locationsL1.add(l1);
        }

        List<TaxiL2Passenger> passengersL1 = new ArrayList<TaxiL2Passenger>();

        for(TaxiPassenger p0 : sL0.passengers){
        	int xp = p0.x;
            int yp = p0.y;
            TaxiL2Passenger p1 = new TaxiL2Passenger(p0.name(), p0.inTaxi, p0.goalLocation, ON_ROAD, p0.pickedUpAtLeastOnce);
            for(TaxiLocation l0 : sL0.locations){
                if(xp==l0.x && yp==l0.y){
                    p1.currentLocation = l0.colour;
                    break;
                }
            }
            passengersL1.add(p1);
        }
        return new TaxiL2State(passengersL1,locationsL1);
    }
}
