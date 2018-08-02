package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;

import java.util.Arrays;
import java.util.Objects;

public class IntegerParameterizedAction implements Action {

    protected String name;
    protected int[] integers;

    public IntegerParameterizedAction(String name, int[] integers){
        this.name = name;
        this.integers = integers;
    }

    @Override
    public String actionName() {
        return name;
    }

    @Override
    public Action copy() {
        return new IntegerParameterizedAction(name, integers.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerParameterizedAction that = (IntegerParameterizedAction) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(integers, that.integers);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(integers);
        return result;
    }

    @Override
    public String toString() {
        return "IntegerParameterizedAction{" +
                "name='" + name + '\'' +
                ", integers=" + Arrays.toString(integers) +
                '}';
    }
}
