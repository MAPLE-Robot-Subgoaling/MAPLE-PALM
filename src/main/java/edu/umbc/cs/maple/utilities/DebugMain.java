package edu.umbc.cs.maple.utilities;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import edu.umbc.cs.maple.state.hashing.bugfix.BugfixHashableStateFactory;
import edu.umbc.cs.maple.taxi.Taxi;
import edu.umbc.cs.maple.taxi.stategenerator.TaxiStateFactory;

public class DebugMain {

    public static void main(String[] args) {

        Taxi taxi = new Taxi();
        SADomain domain = taxi.generateDomain();
        State s = TaxiStateFactory.createClassicState(1);
        int numStates = StateReachability.getReachableStates(s, domain, new BugfixHashableStateFactory(false)).size();
        System.out.println(numStates);

    }
}
