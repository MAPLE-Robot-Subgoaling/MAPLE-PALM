package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Objects;

public class Relation<T extends RelationVariable, S extends RelationVariable> implements Comparable<Relation> {

    protected T left;
    protected S right;
    protected RelationType relationType;

    public Relation(T left, S right, RelationType relationType) {
        this.left = left;
        this.right = right;
        this.relationType = relationType;
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public S getRight() {
        return right;
    }

    public void setRight(S right) {
        this.right = right;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation that = (Relation) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                relationType == that.relationType;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        AttributeRelation that = (AttributeRelation) o;
//        boolean sameSides = Objects.equals(left, that.left) && Objects.equals(right, that.right);
//        boolean oppositeSides = Objects.equals(left, that.right) && Objects.equals(right, that.left);
//        return  (sameSides || oppositeSides) && relationType == that.relationType;
//    }

    @Override
    public int hashCode() {

        return Objects.hash(left, right, relationType);
    }

    @Override
    public String toString() {
        return left + " " + relationType + " " + right;
    }

    @Override
    public int compareTo(Relation that) {
        int a = this.left.compareTo(that.left);
        if (a != 0) { return a; }
        int b = this.right.compareTo(that.right);
        if (b != 0) { return b; }
        return this.relationType.compareTo(that.relationType);
    }

}
