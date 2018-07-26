package edu.umbc.cs.maple.hierarchy.framework;

import burlap.mdp.singleagent.model.FullModel;

public interface ParameterizedFullModel extends FullModel {

    void setParams(String[] params);

}
