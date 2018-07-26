package edu.umbc.cs.maple.hiergen.CAT;

import java.util.Objects;

public class AttributeRelation extends Relation<ObjectAttributePair, RelationVariable> {
    public AttributeRelation(ObjectAttributePair left, RelationVariable right, RelationType relationType) {
        super(left, right, relationType);
    }
}
