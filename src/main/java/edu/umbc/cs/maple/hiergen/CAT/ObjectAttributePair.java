package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Objects;

public class ObjectAttributePair extends RelationVariable {

    private String objectName;
    private String attributeName;

    public ObjectAttributePair(String objectName, String attributeName) {
        this.objectName = objectName;
        this.attributeName = attributeName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return objectName + ":" + attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectAttributePair that = (ObjectAttributePair) o;
        return Objects.equals(objectName, that.objectName) &&
                Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(objectName, attributeName);
    }

    @Override
    public int compareTo(RelationVariable o) {
        if (!(o instanceof ObjectAttributePair)) { return 0; }
        ObjectAttributePair that = (ObjectAttributePair) o;
        int a = this.attributeName.compareTo(that.attributeName);
        if (a != 0) { return a; }
        int b = this.objectName.compareTo(that.objectName);
        if (b != 0) { return b; }
        return 0;
    }

}
