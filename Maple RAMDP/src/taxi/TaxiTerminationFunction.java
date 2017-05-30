package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

import java.util.List;

/**
 * Created by ngopalan.
 */
public class TaxiTerminationFunction implements TerminalFunction {


    @Override
    public boolean isTerminal(State state) {
        List<ObjectInstance> passengerList = ((TaxiState)state).objectsOfClass(TaxiDomain.PASSENGERCLASS);
        List<ObjectInstance> locationList = ((TaxiState)state).objectsOfClass(TaxiDomain.LOCATIONCLASS);
        for(ObjectInstance p:passengerList){
            if(((TaxiPassenger)p).inTaxi){
                return false;
            }
            String goalLocation = ((TaxiPassenger)p).goalLocation;
            for(ObjectInstance l :locationList){
//                System.out.println("goal: " + goalLocation);
//                System.out.println("location attribute: " + l.getStringValForAttribute(TaxiDomain.LOCATIONATT));
                if(goalLocation.equals(((TaxiLocation)l).colour)){
                    if(((TaxiLocation)l).x==((TaxiPassenger)p).x
                            && ((TaxiLocation)l).y==((TaxiPassenger)p).y && ((TaxiPassenger)p).pickedUpAtLeastOnce){
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
