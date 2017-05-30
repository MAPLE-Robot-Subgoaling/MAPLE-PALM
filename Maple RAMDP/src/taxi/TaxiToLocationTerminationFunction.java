package taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

import java.util.List;

/**
 * Created by ngopalan.
 */
public class TaxiToLocationTerminationFunction implements TerminalFunction {

    TaxiLocation lEnd;
    public TaxiToLocationTerminationFunction(TaxiLocation l) {
        this.lEnd = l;
    }

    @Override
    public boolean isTerminal(State state) {
        TaxiState sTemp = (TaxiState)state;
        TaxiAgent taxi = sTemp.taxi;
        int taxiX = taxi.x;
        int taxiY = taxi.y;

        if(taxiX==lEnd.x && taxiY==lEnd.y ){
            return true;
        }
        return false;
    }


}
