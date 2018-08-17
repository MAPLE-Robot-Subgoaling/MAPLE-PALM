package edu.umbc.cs.maple.utilities;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;

public interface ConfidenceModel {

    public double getModelReward(HashableState hs, Action a);

    public double getModelTransition(HashableState hs, Action a, HashableState hsp);

    public double getRewardBound(HashableState hs, Action a);

    public double getTransitionBound(HashableState hs, Action a);
}
