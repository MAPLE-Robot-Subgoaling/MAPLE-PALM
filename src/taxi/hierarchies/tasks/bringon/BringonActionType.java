package taxi.hierarchies.tasks.bringon;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import taxi.hierarchies.tasks.bringon.state.TaxiBringonState;

public class BringonActionType implements ActionType {
    //the pickup actions are for pickup up a passenger from the current depot

    public String typeName() {
        return TaxiBringonDomain.ACTION_BRINGON;
    }

    @Override
    public BringonAction associatedAction(String strRep) {
        String pass = strRep.split("_")[1];
        return new BringonAction(pass);
    }

    //there is a action for each passenger in the current configuration
    @Override
    public List<Action> allApplicableActions(State s) {
        TaxiBringonState state = (TaxiBringonState) s;
        List<Action> acts = new ArrayList<Action>();
        String taxi_loc = (String)state.getTaxiAtt(TaxiBringonDomain.ATT_CURRENT_LOCATION);

        for(String pass : state.getPassengers()){
            String pass_loc = (String)state.getPassengerAtt(pass, TaxiBringonDomain.ATT_CURRENT_LOCATION);
            if(pass_loc.equals(taxi_loc)) {
                acts.add(new BringonAction(pass));
            }
        }

        return acts;
    }

    //each navigate action is given a goal
    public class BringonAction implements Action {

        private String passenger;

        public BringonAction(String passenger) {
            this.passenger = passenger;
        }

        public String getPassenger(){
            return passenger;
        }

        @Override
        public String actionName() {
            return TaxiBringonDomain.ACTION_BRINGON + "_" + passenger;
        }

        @Override
        public Action copy() {
            return new BringonAction(passenger);
        }

        @Override
        public String toString(){
            return actionName();
        }

        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            if(other == null || getClass() != other.getClass()) return false;

            BringonAction a = (BringonAction) other;

            return a.passenger.equals(passenger);
        }

        @Override
        public int hashCode(){
            return actionName().hashCode();
        }
    }
}

