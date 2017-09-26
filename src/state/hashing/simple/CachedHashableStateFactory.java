package state.hashing.simple;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

public class CachedHashableStateFactory implements HashableStateFactory {

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

        if(identifierIndependent){
            return new IICachedHashableState(s);
        }
        return new IDCachedHashableState(s);
    }


    public boolean objectIdentifierIndependent() {
        return this.identifierIndependent;
    }
}
