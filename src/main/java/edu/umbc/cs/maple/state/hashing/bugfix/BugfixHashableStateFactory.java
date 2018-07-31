package edu.umbc.cs.maple.state.hashing.bugfix;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;


public class BugfixHashableStateFactory implements HashableStateFactory {

    /**
     * Whether state evaluations of OO-MDPs are object identifier independent (the names of objects don't matter). By
     * default it is independent.
     */
    protected boolean identifierIndependent = true;


    /**
     * Default constructor: object identifier independent and no hash code caching.
     */
    public BugfixHashableStateFactory(){

    }

    /**
     * Initializes with no hash code caching.
     * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s are object identifier independent; if false then dependent.
     */
    public BugfixHashableStateFactory(boolean identifierIndependent){
        this.identifierIndependent = identifierIndependent;
    }


    @Override
    public HashableState hashState(State s) {
        if(s instanceof IIBugfixHashableState || s instanceof IDBugfixHashableState){
            return (HashableState)s;
        }

        if(identifierIndependent){
            return new IIBugfixHashableState(s);
        }
        return new IDBugfixHashableState(s);
    }


    public boolean objectIdentifierIndependent() {
        return this.identifierIndependent;
    }





}