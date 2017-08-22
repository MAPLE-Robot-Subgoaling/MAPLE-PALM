package taxi.hierarchies.tasks.put;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.put.state.TaxiPutState;

public class DropoffActionType implements ActionType {

    public String typeName() {
        return TaxiPutDomain.ACTION_DROPOFF;
    }

    @Override
    public DropoffAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new DropoffAction(pass);
    }

    //there is a action for each passenger in the current configuration
    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiPutState state = (TaxiPutState) s;
        List<Action> acts = new ArrayList<Action>();

        String taxiLoc = (String)state.getTaxiAtt(TaxiPutDomain.ATT_TAXI_LOCATION);
        for(String pass : state.getPassengers()){
            boolean inTaxi = (boolean)state.getPassengerAtt(pass, TaxiPutDomain.ATT_IN_TAXI);
            // Can only dropoff if we're in the taxi and at a depot
            if(inTaxi && !taxiLoc.equals(TaxiPutDomain.ON_ROAD)) {
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
            return TaxiPutDomain.ACTION_DROPOFF + "_" + passenger;
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
