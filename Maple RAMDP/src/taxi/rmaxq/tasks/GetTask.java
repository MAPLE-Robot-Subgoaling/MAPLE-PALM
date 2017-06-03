package taxi.rmaxq.tasks;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.rmaxq.tasks.GetTask.GetType.GetAction;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

public class GetTask extends NonprimitiveTask{

	public static final String ACTION_GET = "get";
	
	public GetTask(Task[] children) {
		super(children, new GetType(), null, null);
		setRF(new GetRF());
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		GetAction action = (GetAction) a;
		String passName = action.passenger;
	   	TaxiState st = (TaxiState)s;
	   	TaxiPassenger p = st.touchPassenger(passName);
	   	
	   	return p.inTaxi || st.taxi.taxiOccupied;
	}

    public static class GetType implements ActionType {
        @Override
        public String typeName() {
            return ACTION_GET;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new GetAction(strRep);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiPassenger> passengers = ((TaxiState) s).passengers;
            for (TaxiPassenger passenger : passengers) {
                actions.add(new GetAction(passenger.name()));

            }
            return actions;
        }

        public static class GetAction implements Action {

            public String passenger;

            public GetAction(String passenger) {
                this.passenger = passenger;
            }

            @Override
            public String actionName() {
                return ACTION_GET + "_" + passenger;
            }

            @Override
            public Action copy() {
                return new GetAction(passenger);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                GetAction that = (GetAction) o;

                return that.passenger.equals(passenger);

            }

            @Override
            public int hashCode() {
                String str = ACTION_GET + "_" + passenger;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }
    }
    
    public class GetRF implements RewardFunction{

		@Override
		public double reward(State s, Action a, State sprime) {
			GetAction action = (GetAction) a;
			String passName = action.passenger;
		   	TaxiState st = (TaxiState)s;
		   	TaxiPassenger p = st.touchPassenger(passName);
		   	
		   	if(p.inTaxi)
		   		return 1;
		   	else 
		   		return 0;
		}
    }
}
