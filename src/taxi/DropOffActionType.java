package taxi;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class DropOffActionType implements ActionType {

    public String typeName() {
        return Taxi.ACTION_DROPOFF;
    }

    @Override
    public DropOffAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new DropOffAction(pass);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiState state = (TaxiState) s;
        List<Action> acts = new ArrayList<>();
        boolean taxiOccupied = (boolean) state.getTaxiAtt(Taxi.ATT_TAXI_OCCUPIED);

        if (taxiOccupied) {

            for (String pass : state.getPassengers()) {
                boolean inTaxi = (boolean) state.getPassengerAtt(pass, Taxi.ATT_IN_TAXI);
                if (inTaxi) {
                    acts.add(new DropOffAction(pass));
                }
            }
        }

        return acts;
    }

    //each navigate action is given a goal
    public class DropOffAction implements Action {

        private String passenger;

        public DropOffAction(String passenger) {
            this.passenger = passenger;
        }

        public String getPassenger(){
            return passenger;
        }

        @Override
        public String actionName() {
            return Taxi.ACTION_DROPOFF + "_" + passenger;
        }

        @Override
        public Action copy() {
            return new DropOffAction(passenger);
        }

        @Override
        public String toString(){
            return actionName();
        }

        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            if(other == null || getClass() != other.getClass()) return false;

            DropOffAction a = (DropOffAction) other;

            return a.passenger.equals(passenger);
        }

        @Override
        public int hashCode(){
            return actionName().hashCode();
        }
    }


}
