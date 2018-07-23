package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Objects;

public class RelationConstant extends RelationVariable {

    private Object value;

    public RelationConstant() {

    }

    public RelationConstant(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationConstant that = (RelationConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
