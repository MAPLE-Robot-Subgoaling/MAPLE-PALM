package taxi.rmaxq.tasks;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.amdp.level2.state.TaxiL2Location;
import taxi.amdp.level2.state.TaxiL2Passenger;
import taxi.amdp.level2.state.TaxiL2State;
import taxi.rmaxq.tasks.PutTask.PutType.PutAction;
import taxi.state.TaxiLocation;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

public class PutTask extends NonprimitiveTask{
	public final static String ACTION_PUT = "put";
	public PutTask(Task[] children) {
		super(children, new PutType(), null, null);
		setRF(new putRF());
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		PutAction action = (PutAction) a;
		TaxiState state = (TaxiState) s;
		String goalLocation = action.location;
		String passname = action.passenger;
		TaxiPassenger p = state.touchPassenger(passname);
		if(!state.taxi.taxiOccupied)
			return true;

		for(TaxiLocation l : state.locations){
			if(p.x == l.x && p.y == l.y){
				if(goalLocation.equals(l.colour)){
					return true;
				}
			}
		}
		return false;
	}

	public static class PutType implements ActionType {

    	@Override
        public String typeName() {
            return ACTION_PUT;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new PutAction(strRep.split("_")[0], strRep.split("_")[1]);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiPassenger> passengers = ((TaxiState) s).passengers;
            List<TaxiLocation> locations = ((TaxiState) s).locations;
            for (TaxiPassenger passenger : passengers) {
                for (TaxiLocation loc : locations) {
                    actions.add(new PutAction(passenger.name(), loc.colour));
                }

            }
            return actions;
        }

        public static class PutAction implements Action {

            public String passenger;
            public String location;

            public PutAction(String passenger, String location) {
                this.passenger = passenger;
                this.location = location;
            }

            @Override
            public String actionName() {
                return ACTION_PUT + "_" + passenger + "_" + location;
            }

            @Override
            public Action copy() {
                return new PutAction(passenger, location);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                PutAction that = (PutAction) o;

                return that.passenger.equals(passenger) && that.location.equals(location);

            }

            @Override
            public int hashCode() {
                String str = ACTION_PUT + "_" + passenger + "_" + location;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }
    }
	
	public class putRF implements RewardFunction{

		@Override
		public double reward(State s, Action a, State sprime) {
			PutAction action = (PutAction) a;
			TaxiState state = (TaxiState) s;
			String goalLocation = action.location;
			String passname = action.passenger;
			TaxiPassenger p = state.touchPassenger(passname);

			for(TaxiLocation l : state.locations){
				if(p.x == l.x && p.y == l.y){
					if(goalLocation.equals(l.colour)){
						if(!p.inTaxi && p.pickedUpAtLeastOnce){
							return 1;
						}
					}
				}
			}
			
			return 0;
		}
		
	}
}
