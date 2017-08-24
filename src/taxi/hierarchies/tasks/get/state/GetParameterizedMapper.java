package taxi.hierarchies.tasks.get.state;

import burlap.mdp.core.state.State;

import taxi.Taxi;
import taxi.hierarchies.tasks.get.TaxiGetDomain;
import taxi.state.TaxiState;
import taxi.hierarchies.interfaces.ParameterizedStateMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sparr on 8/21/17.
 */
public class GetParameterizedMapper implements ParameterizedStateMapping{
    @Override
    public State mapState(State s, String...params){
        List<TaxiGetPassenger> passengers = new ArrayList<TaxiGetPassenger>();
        List<TaxiGetLocation> locations = new ArrayList<TaxiGetLocation>();
        TaxiState st = (TaxiState) s;

        // Get Taxi
        String taxiLocation = TaxiGetDomain.ON_ROAD;
        int tx = (int)st.getTaxiAtt(Taxi.ATT_X);
        int ty = (int)st.getTaxiAtt(Taxi.ATT_Y);
        for (String locName : st.getLocations()) {
            int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
            int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);

            locations.add(new TaxiGetLocation(locName));

            if (tx == lx && ty == ly) {
                taxiLocation = locName;
            }
        }
        TaxiGetAgent taxi = new TaxiGetAgent(TaxiGetDomain.CLASS_TAXI, taxiLocation);

        // Get Passengers from parameters
        for(String passengerName : params){
            int px = (int) st.getPassengerAtt(passengerName, Taxi.ATT_X);
            int py = (int) st.getPassengerAtt(passengerName, Taxi.ATT_Y);
            boolean inTaxi = (boolean) st.getPassengerAtt(passengerName, Taxi.ATT_IN_TAXI);
            String passengerLocation = TaxiGetDomain.IN_TAXI;

            if(!inTaxi) {
                for (String locName : st.getLocations()) {
                    int lx = (int) st.getLocationAtt(locName, Taxi.ATT_X);
                    int ly = (int) st.getLocationAtt(locName, Taxi.ATT_Y);

                    if (px == lx && py == ly) {
                        passengerLocation = locName;
                        break;
                    }
                }
            }
            passengers.add(new TaxiGetPassenger(passengerName, passengerLocation));
        }

        return new TaxiGetState(taxi, passengers, locations);
    }

    @Override
    public State mapState(State s) {
        return mapState(s,((TaxiState)s).getPassengers());
    }
}
