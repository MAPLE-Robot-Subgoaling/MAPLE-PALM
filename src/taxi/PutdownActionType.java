package taxi;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.dropoff.TaxiDropoffDomain;
import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

public class PutdownActionType implements ActionType {

    public String typeName() {
        return Taxi.ACTION_PUTDOWN;
    }

    @Override
    public PutdownAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new PutdownAction(pass);
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiDropoffState state = (TaxiDropoffState) s;
        List<Action> acts = new ArrayList<>();

        for (String pass : state.getPassengers()) {
            String pass_loc = (String)state.getPassengerAtt(pass, TaxiDropoffDomain.ATT_LOCATION);
            boolean notInTaxi = pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI);
            if (!notInTaxi) {
                acts.add(new PutdownAction(pass));
            }
        }

        return acts;
    }

    //each navigate action is given a goal
    public class PutdownAction implements Action {

        private String passenger;

        public PutdownAction(String passenger) {
            this.passenger = passenger;
        }

        public String getPassenger(){
            return passenger;
        }

        @Override
        public String actionName() {
            return Taxi.ACTION_PUTDOWN + "_" + passenger;
        }

        @Override
        public Action copy() {
            return new PutdownAction(passenger);
        }

        @Override
        public String toString(){
            return actionName();
        }

        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            if(other == null || getClass() != other.getClass()) return false;

            PutdownAction a = (PutdownAction) other;

            return a.passenger.equals(passenger);
        }

        @Override
        public int hashCode(){
            return actionName().hashCode();
        }
    }


}
