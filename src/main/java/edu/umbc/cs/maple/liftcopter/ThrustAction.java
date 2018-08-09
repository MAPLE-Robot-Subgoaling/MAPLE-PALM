package edu.umbc.cs.maple.liftcopter;

import burlap.mdp.core.action.Action;

import java.util.Objects;

public class ThrustAction implements Action {
    public double thrust;
    public double direction;
    String typeName = "thrust";

    public ThrustAction() {
        // for de/serialization
    }

    public ThrustAction(double thrust, double direction) {
        this.thrust = thrust;
        this.direction = direction;
        typeName = "thrust|" + this.thrust + "|" + (this.direction/Math.PI);
    }

    public String actionName() {
        return "thrust|" + this.thrust + "|" + (this.direction/Math.PI);
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