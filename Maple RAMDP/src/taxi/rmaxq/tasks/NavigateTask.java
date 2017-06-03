package taxi.rmaxq.tasks;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import hierarchy.framework.NonprimitiveTask;
import hierarchy.framework.Task;
import taxi.rmaxq.tasks.NavigateTask.NavigateType.NavigateAction;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiState;

public class NavigateTask extends NonprimitiveTask{
	
	public static String ACTION_NAVIGATE = "nacigate";
	public NavigateTask(Task[] children) {
		super(children, new NavigateType(), null, null);
	}

	@Override
	public boolean isTerminal(State s, Action a) {
		TaxiState state = (TaxiState) s;
		NavigateAction action = (NavigateAction) a;
		String goalLocation = action.location;
		TaxiLocation goal = state.touchLocation(state.locationIndWithColour(goalLocation));
		TaxiAgent t = state.taxi;
		
		return t.x == goal.x && t.y == goal.y;
	}

    public static class NavigateType implements ActionType {
        public String typeName() {
            return ACTION_NAVIGATE;
        }

        public Action associatedAction(String strRep) {
            return new NavigateAction(strRep);
        }

        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiLocation> locations = ((TaxiState)s).locations;

            for(TaxiLocation location: locations){
            	actions.add(new NavigateAction(location.colour));
            }
            return actions;
        }

        public static class NavigateAction implements Action{

            public String location;

            public NavigateAction(String location) {
                this.location= location;
            }

            @Override
            public String actionName() {
                return ACTION_NAVIGATE + "_" + location;
            }

            @Override
            public Action copy() {
                return new NavigateAction(location);
            }

            @Override
            public boolean equals(Object o) {
                if(this == o) return true;
                if(o == null || getClass() != o.getClass()) return false;

                NavigateAction that = (NavigateAction) o;

                return that.location.equals(location) ;

            }

            @Override
            public int hashCode() {
                String str = ACTION_NAVIGATE + "_" + location;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }
    }
}
