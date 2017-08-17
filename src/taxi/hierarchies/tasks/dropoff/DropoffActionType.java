package taxi.hierarchies.tasks.dropoff;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;

public class DropoffActionType implements ActionType {

    public String typeName() {
        return TaxiDropoffDomain.ACTION_DROPOFF;
    }

    @Override
    public DropoffAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new DropoffAction(pass);
    }

    //there is a action for each passenger in the current configuration
    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiDropoffState state = (TaxiDropoffState) s;
        List<Action> acts = new ArrayList<Action>();
        String taxi_loc = (String)state.getTaxiAtt(TaxiDropoffDomain.ATT_CURRENT_LOCATION);

        for(String pass : state.getPassengers()){
            String pass_loc = (String)state.getPassengerAtt(pass, TaxiDropoffDomain.ATT_CURRENT_LOCATION);
            if(pass_loc.equals(taxi_loc) && (boolean)state.getPassengerAtt(pass, TaxiDropoffDomain.ATT_IN_TAXI)) {
                acts.add(new DropoffAction(pass));
            }
        }

        return acts;
    }

    //each navigate action is given a goal
    public class DropoffAction implements Action {

        private String passenger;

        public DropoffAction(String passenger) {
            this.passenger = passenger;
        }

        public String getPassenger(){
            return passenger;
        }

        @Override
        public String actionName() {
            return TaxiDropoffDomain.ACTION_DROPOFF + "_" + passenger;
        }

        @Override
        public Action copy() {
            return new DropoffAction(passenger);
        }

        @Override
        public String toString(){
            return actionName();
        }

        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            if(other == null || getClass() != other.getClass()) return false;

            DropoffAction a = (DropoffAction) other;

            return a.passenger.equals(passenger);
        }

        @Override
        public int hashCode(){
            return actionName().hashCode();
        }
    }
}
