package taxi;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import taxi.state.TaxiAgent;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;

import java.util.ArrayList;
import java.util.List;

import static taxi.TaxiConstants.*;
public class TaxiModel implements FullStateModel{

    /**
     * the array saying how the probabilities are distributed
     */
    private double[][] moveProbability;

    /**
     * transitionProbability the passenger just picked up changes their goal
     */
    private double fickleChangeGoalProbaility;

    /**
     * whether the passengers are fickle
     */
    private boolean fickle;

    /**
     * create a taxi model
     * @param moveprob array of movement probabilities
     * @param fickle whether passengers are fickle
     * @param fickleprob transitionProbability the passengers are fickle
     */
    public TaxiModel(double[][] moveprob, boolean fickle, double fickleprob) {
        this.moveProbability = moveprob;
        this.fickleChangeGoalProbaility = fickleprob;
        this.fickle = fickle;
    }

    /**
     * creates a stochastic non fickle taxi model
     * @param moveprob the array of movement probabilities
     */
    public TaxiModel(double[][] moveprob){
        this.moveProbability = moveprob;
        this.fickle = false;
    }

    @Override
    public State sample(State s, Action a) {
         List<StateTransitionProb> stpList = this.stateTransitions(s,a);
         double roll = RandomFactory.getMapped(0).nextDouble();
//         System.out.println(roll);
         double curSum = 0.;
         for(int i = 0; i < stpList.size(); i++){
             curSum += stpList.get(i).p;
             if(roll < curSum){
                 return stpList.get(i).s;
             }
         }
         throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>();
        int action = actionInd(a);
        TaxiState taxiS = (TaxiState) s;

        if(action <= IND_WEST){
            movement(taxiS, action, tps);
        }else if(action == IND_PUTDOWN){
            putdown(taxiS, (ObjectParameterizedAction)a, tps);
        }else if(action == IND_PICKUP){
            pickup(taxiS, (ObjectParameterizedAction)a, tps);
        }

        return tps;
    }

    /**
     * add all resulting states given a movement action
     * @param s the current state
     * @param action the index of the selected movement action
     * @param tps a list of state transition probabilities to add to
     */
    public void movement(TaxiState s, int action, List<StateTransitionProb> tps){
        double[] moveProbabilities = this.moveProbability[action];

        int tx = (int) s.getTaxi().get(ATT_X);
        int ty = (int) s.getTaxi().get(ATT_Y);

        for(int outcome = 0; outcome < moveProbabilities.length; outcome++){
            double p = moveProbabilities[outcome];
            if(p == 0)
                continue;

            int dx = 0, dy = 0;
            TaxiState ns = s.copy();

            //move in the given direction unless there are walls in the way
            if(outcome == IND_NORTH){
                if(!ns.wallNorth()){
                    dy = +1;
                }
            }else if(outcome == IND_EAST){
                if(!ns.wallEast()){
                    dx = +1;
                }
            }else if(outcome == IND_SOUTH){
                if(!ns.wallSouth()){
                    dy = -1;
                }
            }else if(outcome == IND_WEST){
                if(!ns.wallWest()){
                    dx = -1;
                }
            }

            boolean somethingChanged = dx != 0 || dy != 0;
            if (somethingChanged) {
                int nx = tx + dx;
                int ny = ty + dy;
                TaxiAgent ntaxi = ns.touchTaxi();
                ntaxi.set(ATT_X, nx);
                ntaxi.set(ATT_Y, ny);

                //move any passenger that are in the taxi
                for(ObjectInstance passenger : s.objectsOfClass(CLASS_PASSENGER)){
                    boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
                    if(inTaxi){
                        String passengerName = passenger.name();
                        TaxiPassenger np = ns.touchPassenger(passengerName);
                        np.set(ATT_X, nx);
                        np.set(ATT_Y, ny);
                    }
                }
            }


            if(fickle){
                //find a passenger in the taxi
                boolean passengerChanged = false;
                for(ObjectInstance passenger : s.objectsOfClass(CLASS_PASSENGER)){
                    boolean inTaxi = (boolean) passenger.get(ATT_IN_TAXI);
                    String passGoal = (String) passenger.get(ATT_GOAL_LOCATION);
                    if(inTaxi){
                        passengerChanged = true;
                        //TaxiPassenger np = ns.touchPassenger(passengerName);
                        //np.set(ATT_JUST_PICKED_UP, false);
                        // may change goal
                        for(ObjectInstance location : s.objectsOfClass(CLASS_LOCATION)){
                            TaxiState nfickles = ns.copy();

                            //check if goal is the same as loc
                            if(passGoal.equals(location.name())){
                                double prob = p * (1 - fickleChangeGoalProbaility);
                                tps.add(new StateTransitionProb(nfickles, prob));
                            }else{
                                //set goal to loc
                                TaxiPassenger npf = nfickles.touchPassenger(passenger.name());
                                npf.set(ATT_GOAL_LOCATION, location.name());
                                tps.add(new StateTransitionProb(nfickles, p * (fickleChangeGoalProbaility
                                        / (s.objectsOfClass(CLASS_LOCATION).size() - 1))));
                            }
                        }
                        break;
                    }
                }
                if(!passengerChanged){
                    tps.add(new StateTransitionProb(ns, p));
                }
            }else{
                tps.add(new StateTransitionProb(ns, p));
            }
        }
    }

    /**
     * put passenger at the taxi inside if no one else is inside
     * @param s the current state
     * @param a the passenger parameterized pickup action
     * @param tps a list of state transition probabilities to add to
     */
    public void pickup(TaxiState s, ObjectParameterizedAction a, List<StateTransitionProb> tps) {
        String p = a.getObjectParameters()[0];
        TaxiState ns = s.copy();

        int tx = (int) s.getTaxi().get(ATT_X);
        int ty = (int) s.getTaxi().get(ATT_Y);

        int px = (int) s.object(p).get(ATT_X);
        int py = (int) s.object(p).get(ATT_Y);
        boolean inTaxi = (boolean) s.object(p).get(ATT_IN_TAXI);

        if (tx == px && ty == py && !inTaxi) {
            TaxiPassenger np = ns.touchPassenger(p);
            np.set(ATT_IN_TAXI, true);

//			TaxiAgent ntaxi = ns.touchTaxi();
//			ntaxi.set(ATT_TAXI_OCCUPIED, true);
        }
        tps.add(new StateTransitionProb(ns, 1.));
    }

    /**
     * put passenger down if the taxi is occupied and at a depot
     * @param s the current state
     * @param a the passenger parameterized putdown action
     * @param tps a list of state transition probabilities to add to
     */
    public void putdown(TaxiState s, ObjectParameterizedAction a, List<StateTransitionProb> tps){
        String p = a.getObjectParameters()[0];
        TaxiState ns = s.copy();
        int tx = (int) s.getTaxi().get(ATT_X);
        int ty = (int) s.getTaxi().get(ATT_Y);

        if((boolean) s.object(p).get(ATT_IN_TAXI)) {
            for (ObjectInstance location : s.objectsOfClass(CLASS_LOCATION)) {
                int lx = (int) location.get(ATT_X);
                int ly = (int) location.get(ATT_Y);
                if (tx == lx && ty == ly) {
                    TaxiPassenger np = ns.touchPassenger(p);
                    np.set(ATT_IN_TAXI, false);
                    break;
                }
            }
        }

        tps.add(new StateTransitionProb(ns, 1.));
    }

    /**
     * map a action to its number
     * @param a the action
     * @return the number that represents the action
     */
    public int actionInd(Action a){
        String aname = a.actionName();
        if(aname.startsWith(ACTION_NORTH))
            return IND_NORTH;
        else if(aname.startsWith(ACTION_EAST))
            return IND_EAST;
        else if(aname.startsWith(ACTION_SOUTH))
            return IND_SOUTH;
        else if(aname.startsWith(ACTION_WEST))
            return IND_WEST;
        else if(aname.startsWith(ACTION_PICKUP))
            return IND_PICKUP;
        else if(aname.startsWith(ACTION_PUTDOWN))
            return IND_PUTDOWN;
        throw new RuntimeException("Invalid action " + aname);
    }
}
