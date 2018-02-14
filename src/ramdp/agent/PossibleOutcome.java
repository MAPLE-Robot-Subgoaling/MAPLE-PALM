package ramdp.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

public class PossibleOutcome {

    protected HashableStateFactory hashingFactory;
    protected EnvironmentOutcome outcome;
    protected TransitionProb transitionProbability;

    public PossibleOutcome(HashableStateFactory hashingFactory, EnvironmentOutcome outcome, double probability) {
        this.hashingFactory = hashingFactory;
        this.outcome = outcome;
        this.transitionProbability = new TransitionProb(probability, this.outcome);
    }

    public EnvironmentOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(EnvironmentOutcome outcome) {
        this.outcome = outcome;
        this.transitionProbability.eo = this.outcome;
    }

    public HashableStateFactory getHashingFactory() {
        return hashingFactory;
    }

    public void setHashingFactory(HashableStateFactory hashingFactory) {
        this.hashingFactory = hashingFactory;
    }

    public double getTransitionProbability() {
        return transitionProbability.p;
    }

    public void setTransitionProbability(double transitionProbability) {
        this.transitionProbability.p = transitionProbability;
    }

    public double getReward() {
        return this.outcome.r;
    }

    public void setReward(double reward) {
        this.outcome.r = reward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (outcome != null && transitionProbability != null && transitionProbability.eo != null && outcome != transitionProbability.eo) {
            throw new RuntimeException("ERROR: the EnvironmentOutcome stored in the TransitionProb was not identical to the one in its own PossibleOutcome");
        }

        PossibleOutcome that = (PossibleOutcome) o;

        if (transitionProbability.p != that.transitionProbability.p) {
            return false;
        }

        if (hashingFactory != null ? !hashingFactory.equals(that.hashingFactory) : that.hashingFactory != null) {
            return false;
        }

        // handle equality by the State x Action x StatePrime components of the outcome, hashed
        HashableState hs = hashingFactory.hashState(outcome.o);
        HashableState thatHs = that.hashingFactory.hashState(that.outcome.o);
        if(hs != null ? !hs.equals(thatHs) : thatHs != null) {
            return false;
        }

        Action action = outcome.a;
        Action thatAction = that.outcome.a;
        if(action != null ? !action.equals(thatAction) : thatAction != null) {
            return false;
        }

        HashableState hsPrime = hashingFactory.hashState(outcome.op);
        HashableState thatHsPrime = that.hashingFactory.hashState(that.outcome.op);
        if(hsPrime != null ? !hsPrime.equals(thatHsPrime) : thatHsPrime != null) {
            return false;
        }

        Double r = outcome.r;
        Double thatR = that.outcome.r;
        if(r != null ? !r.equals(thatR) : thatR != null) {
            return false;
        }

        Boolean terminated = outcome.terminated;
        Boolean thatTerminated = that.outcome.terminated;
        if(terminated != null ? !terminated.equals(thatTerminated) : thatTerminated != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        // hashes and collides on hashedState, action, hashedStatePrime
        int result = hashingFactory != null ? hashingFactory.hashCode() : 0;
        HashableState hs = hashingFactory.hashState(outcome.o);
        Action action = outcome.a;
        HashableState hsPrime = hashingFactory.hashState(outcome.op);
        result = 31 * result + (hs != null ? hs.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (hsPrime != null ? hsPrime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PossibleOutcome{" +
                "p=" + getTransitionProbability() +
                ", r=" + getReward() +
                ", s=" + outcome.o +
                ", a=" + outcome.a +
                ", sp=" + outcome.op +
                '}';
    }
}
