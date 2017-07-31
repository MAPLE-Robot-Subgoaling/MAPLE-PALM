package taxi.abstraction1;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.abstraction1.state.TaxiL1State;

public class PickupActionType implements ActionType {
    //the pickup actions are for pickup up a passenger from the current depot

    public String typeName() {
        return TaxiL1.ACTION_L1PICKUP;
    }

    @Override
    public PickupAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new PickupAction(pass);
    }

    //there is a action for each passenger in the current configuration
    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiL1State state = (TaxiL1State) s;
        List<Action> acts = new ArrayList<Action>();
        String taxi_loc = (String)state.getTaxiAtt(TaxiL1.ATT_CURRENT_LOCATION);

        for(String pass : state.getPassengers()){
            String pass_loc = (String)state.getPassengerAtt(pass, TaxiL1.ATT_CURRENT_LOCATION);
            if(pass_loc.equals(taxi_loc)) {
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
            return TaxiL1.ACTION_L1PICKUP + "_" + passenger;
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

