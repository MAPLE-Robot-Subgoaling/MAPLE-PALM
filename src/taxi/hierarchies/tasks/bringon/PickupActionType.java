package taxi.hierarchies.tasks.bringon;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class PickupActionType implements ActionType {
    // the pickup actions for picking up a passenger at the current location

    public String typeName() {
        return Taxi.ACTION_PICKUP;
    }

    @Override
    public PickupAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new PickupAction(pass);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiState state = (TaxiState)s;
        List<Action> acts = new ArrayList<>();
        int taxi_x = (int)state.getTaxiAtt(Taxi.ATT_X);
        int taxi_y = (int)state.getTaxiAtt(Taxi.ATT_Y);

        for(String pass : state.getPassengers()){
            int pass_x = (int)state.getPassengerAtt(pass, Taxi.ATT_X);
            int pass_y = (int)state.getPassengerAtt(pass, Taxi.ATT_Y);
            if(pass_x == taxi_x && pass_y == taxi_y) {
                acts.add(new PickupAction(pass));
            }
        }

        return acts;
    }

    //each navigate action is given a goal
    public class PickupAction implements Action {

        private String passenger;

        public PickupAction(String passenger) {
            this.passenger = passenger;
        }

        public String getPassenger(){
            return passenger;
        }

        @Override
        public String actionName() {
            return Taxi.ACTION_PICKUP + "_" + passenger;
        }

        @Override
        public Action copy() {
            return new PickupAction(passenger);
        }

        @Override
        public String toString(){
            return actionName();
        }

        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            if(other == null || getClass() != other.getClass()) return false;

            PickupAction a = (PickupAction) other;

            return a.passenger.equals(passenger);
        }

        @Override
        public int hashCode(){
            return actionName().hashCode();
        }
    }
}

