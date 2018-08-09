package edu.umbc.cs.maple.state.hashing.bugfix;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.discretized.DiscConfig;
import weka.Run;

public class FixDiscretizedHashableStateFactory extends BugfixHashableStateFactory {

    /**
     * The discretization config
     */
    protected DiscConfig config;


    public FixDiscretizedHashableStateFactory() {
        // for de/serialization
    }


    /**
     * Initializes with object identifier independence and no hash code caching.
     * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
     */
    public FixDiscretizedHashableStateFactory(double defaultMultiple) {
        this.config = new DiscConfig(defaultMultiple);
    }


    /**
     * Initializes with non hash code caching
     * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
     * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
     */
    public FixDiscretizedHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
        super(identifierIndependent);
        config = new DiscConfig(defaultMultiple);
    }


    /**
     * Sets the multiple to use for discretization for the given key. See the class documentation
     * for more information on how the multiple works.
     * @param key the name of the state variable key whose discretization multiple is being set.
     * @param nearestMultipleValue the multiple to which values are floored.
     */
    public void addFloorDiscretizingMultipleFor(Object key, double nearestMultipleValue){
        config.keyWiseMultiples.put(key, nearestMultipleValue);
    }


    /**
     * Sets the default multiple to use for continuous values that do not have specific multiples set
     * for them. See the documentation
     * of this class for more information on how the multiple works. In short, continuous values will be floored
     * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
     * @param defaultMultiple the default multiple to which values are floored
     */
    public void setDefaultFloorDiscretizingMultiple(double defaultMultiple){
        config.defaultMultiple = defaultMultiple;
    }

    @Override
    public HashableState hashState(State s) {
        if(s instanceof IIBugfixHashableState || s instanceof IDBugfixHashableState){
            return (HashableState)s;
        }

        if(identifierIndependent){
            throw new RuntimeException("not implemented");
        }
        return new IDFixDiscretizedHashableState(s, config);
    }
}