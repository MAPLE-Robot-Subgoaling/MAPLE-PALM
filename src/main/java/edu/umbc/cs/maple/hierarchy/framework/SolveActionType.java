package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

public class SolveActionType implements ActionType{

    @Override
    public String typeName() {
        return "Solve hierarchy";
    }

    @Override
    public Action associatedAction(String strRep) {
        return new SolveAction();
    }

    @Override
    public List<Action> allApplicableActions(State s) {
        List<Action> acts = new ArrayList<Action>();
        acts.add(new SolveAction());
        return acts;
    }

    public class SolveAction implements Action{

        @Override
        public String actionName() {
            return "solve";
        }

        @Override
        public Action copy() {
            return new SolveAction();
        }


        @Override
        public boolean equals(Object other){
            if(this == other) return true;
            return other == null || getClass() != other.getClass();
        }

        @Override
        public String toString() {
            return actionName();
        }

    }
}
