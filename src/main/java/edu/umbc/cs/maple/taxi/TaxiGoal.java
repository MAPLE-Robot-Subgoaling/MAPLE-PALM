package edu.umbc.cs.maple.taxi;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.taxi.functions.amdp.RootCompletedPF;

public class TaxiGoal extends DomainGoal {

    protected TerminalFunction goalTF;

    public TaxiGoal() {
        super("TaxiGoal", new String[]{});
        this.goalTF = new TaxiTerminalFunction();
    }

    @Override
    public boolean satisfies(State state) {
        return false;
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return goalTF.isTerminal(s);
    }

}
