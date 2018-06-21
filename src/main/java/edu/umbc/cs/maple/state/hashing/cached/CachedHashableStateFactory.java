package edu.umbc.cs.maple.state.hashing.cached;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.WrappedHashableState;
import edu.umbc.cs.maple.utilities.DeepCopyForShallowCopyState;

import java.util.HashMap;

public class CachedHashableStateFactory implements HashableStateFactory {

    protected HashMap<Integer, WrappedHashableState> states = new HashMap<>();

    /**
     * Whether state evaluations of OO-MDPs are object identifier independent (the names of objects don't matter). By
     * default it is independent.
     */
    protected boolean identifierIndependent = true;


    /**
     * Default constructor: object identifier independent and no hash code caching.
     */
    public CachedHashableStateFactory(){

    }

    /**
     * Initializes with no hash code caching.
     * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s are object identifier independent; if false then dependent.
     */
    public CachedHashableStateFactory(boolean identifierIndependent){
        this.identifierIndependent = identifierIndependent;
    }


    @Override
    public HashableState hashState(State s) {
        if(s instanceof IICachedHashableState || s instanceof IDCachedHashableState){
            return (HashableState)s;
        }

        if (!(s instanceof DeepCopyForShallowCopyState)) {
            throw new RuntimeException("Error: to use CachedHashing, the state must implement DeepCopyForShallowCopyState");
        }


        DeepCopyForShallowCopyState dcfscs = (DeepCopyForShallowCopyState) s;


//        int hc = s.hashCode();
//        HashableState hs = states.computeIfAbsent(hc, k -> new IDCachedHashableState(dcfscs));
////        State storedS = hs.s();
////        if (hc != 0 && !storedS.equals(s)) {
////            System.err.println("bad");
////            int h1 = storedS.hashCode();
////            int h2 = s.hashCode();
////            System.err.println(h1==h2);
////        }
//        return hs;


        WrappedHashableState hs;
        if(identifierIndependent){
            hs = new IICachedHashableState(dcfscs);
            throw new RuntimeException("Not checked for bugs");
        } else {
            hs = new IDCachedHashableState(dcfscs);
        }
        Integer hashCode = hs.hashCode();
        if (!states.containsKey(hashCode)) {
            states.put(hashCode, hs);
        }
        hs = states.get(hashCode);
        return hs;
    }


    public boolean objectIdentifierIndependent() {
        return this.identifierIndependent;
    }
}
