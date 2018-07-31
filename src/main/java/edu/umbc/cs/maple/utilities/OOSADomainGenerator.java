package edu.umbc.cs.maple.utilities;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.model.RewardFunction;

public abstract class OOSADomainGenerator implements DomainGenerator {
    public abstract void setTf(TerminalFunction tf);
    public abstract void setRf(RewardFunction rf);
    public abstract TerminalFunction getTf();
    public abstract RewardFunction getRf();
}
