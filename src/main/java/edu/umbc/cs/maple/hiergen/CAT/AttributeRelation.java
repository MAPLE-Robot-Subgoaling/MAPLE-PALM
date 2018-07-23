package edu.umbc.cs.maple.hiergen.CAT;

import burlap.mdp.core.oo.state.OOVariableKey;

import java.util.Objects;

public class AttributeRelation {

    private RelationVariable left;
    private RelationVariable right;
    private Relation relation;

    public AttributeRelation(RelationVariable left, RelationVariable right, Relation relation) {
        this.left = left;
        this.right = right;
        this.relation = relation;
    }

    public RelationVariable getLeft() {
        return left;
    }

    public void setLeft(RelationVariable left) {
        this.left = left;
    }

    public RelationVariable getRight() {
        return right;
    }

    public void setRight(RelationVariable right) {
        this.right = right;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeRelation that = (AttributeRelation) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                relation == that.relation;
    }

    @Override
    public int hashCode() {

        return Objects.hash(left, right, relation);
    }

    @Override
    public String toString() {
        return left + " " + relation + " " + right;
    }
}
