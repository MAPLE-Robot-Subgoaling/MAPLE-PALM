package taxi.hierarchies.tasks.bringon;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.Taxi;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;
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
        TaxiBringonState state = (TaxiBringonState)s;
        List<Action> acts = new ArrayList<>();
        String taxi_location = (String)state.getTaxiAtt(TaxiBringonDomain.ATT_LOCATION);

        for(String pass : state.getPassengers()){
            String pass_location = (String)state.getPassengerAtt(pass, TaxiBringonDomain.ATT_LOCATION);
            if(pass_location.equals(taxi_location)) {
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

