package edu.umbc.cs.maple.liftCopter;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ThrustType implements ActionType {
    List<Action> actions;

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
        return "thrust";
    }

    public Action associatedAction(String strRep) {
        String[] tokens = strRep.split("_");
        return new ThrustAction(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2])*Math.PI);
    }

    public List<Action> allApplicableActions(State s) {
        return this.actions;
    }

    public static class ThrustAction implements Action {
        public double thrust;
        public double direction;

        public ThrustAction() {
        }

        public ThrustAction(double thrust, double direction) {
            this.thrust = thrust;
            this.direction = direction;
        }

        public String actionName() {
            return "thrust_" + this.thrust + "_" + (this.direction/Math.PI);
        }

        public Action copy() {
            return new ThrustAction(this.thrust, this.direction);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThrustAction that = (ThrustAction) o;
            return Double.compare(that.thrust, thrust) == 0 &&
                    Double.compare(that.direction, direction) == 0;
        }

        @Override
        public int hashCode() {

            return Objects.hash(thrust, direction);
        }

        public String toString() {
            return this.actionName();
        }
    }
}