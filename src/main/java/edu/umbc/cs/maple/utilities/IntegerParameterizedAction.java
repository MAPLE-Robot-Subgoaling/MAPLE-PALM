package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import edu.umbc.cs.maple.hierarchy.framework.StringFormat;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class IntegerParameterizedAction implements Action {

    protected String name;
    protected int[] integers;

    public IntegerParameterizedAction(String name, int[] integers){
        this.name = name;
        this.integers = integers;
    }

    @Override
    public String actionName() {
        return name + StringFormat.PARAMETER_DELIMITER + Arrays.stream(integers)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(StringFormat.PARAMETER_DELIMITER));
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getIntegers() {
        return integers;
    }

    public void setIntegers(int[] integers) {
        this.integers = integers;
    }

    public String[] getParameters() {
        return Arrays.stream(integers).mapToObj(String::valueOf).toArray(String[]::new);
    }
}
