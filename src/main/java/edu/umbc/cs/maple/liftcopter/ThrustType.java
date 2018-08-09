package edu.umbc.cs.maple.liftcopter;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ThrustType implements ActionType {
    public List<Action> actions;
    public String typeName = "thrust";

    public ThrustType() {
        // for de/serialization
    }

    public ThrustType(List<Double> thrustValues, List<Double> directions) {
        this.actions = new ArrayList(thrustValues.size()*directions.size());
        Iterator tValueIt = thrustValues.iterator();
        Iterator dValueIt;
        while(tValueIt.hasNext()) {
            dValueIt = directions.iterator();
            Double t = (Double)tValueIt.next();
            while(dValueIt.hasNext()){
                Double d = (Double)dValueIt.next();
                this.actions.add(new ThrustAction(t,d));
            }

        }

    }

    public String typeName() {
        return typeName;
    }

    public Action associatedAction(String strRep) {
        String[] tokens = strRep.split("|");
        return new ThrustAction(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2])*Math.PI);
    }

    public List<Action> allApplicableActions(State s) {
        return this.actions;
    }


}