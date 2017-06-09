package taxi.amdp.level2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import taxi.amdp.level2.state.TaxiL2Location;
import taxi.amdp.level2.state.TaxiL2Passenger;
import taxi.amdp.level2.state.TaxiL2State;

/**
 * Created by ngopalan on 8/10/16.
 */
public class TaxiL2Domain implements DomainGenerator {

    public static final String VAR_INTAXI = "inTaxiAtt";
    public static final String VAR_OCCUPIEDTAXI = "occupiedTaxiAtt";
    public static final String VAR_PICKEDUPATLEASTONCE = "pickedUpAtLeastOnce";
    public static final String VAR_LOCATION = "locationAtt";
    public static final String VAR_CURRENTLOCATION = "locationAtt";
    public static final String VAR_GOALLOCATION = "goalLocationAtt";
    public static final String VAR_ORIGINALSOURCELOCATION = "originalSourceLocationAtt";
    public static final String VAR_JUSTPICKEDUP = "justPickedupAtt";

    public static final String LOCATIONL2CLASS = "location";
    public static final String PASSENGERL2CLASS = "passenger";

    public static final String ACTION_GET = "get";
    public static final String ACTION_PUT = "put";

    public static final String ON_ROAD = "on_road";
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String YELLOW = "yellow";
    public static final String DARKGREY = "darkgrey";
    public static final String MAGENTA = "magenta";
    public static final String PINK = "pink";
    public static final String ORANGE = "orange";
    public static final String CYAN = "cyan";
    public static final String FUEL = "fuel";

    protected RandomFactory randomFactory = new RandomFactory();
    protected Random rand;
    protected RewardFunction rf;
    protected TerminalFunction tf;

    public TaxiL2Domain(RewardFunction rf, TerminalFunction tf, Random rand) {
        this.rf = rf;
        this.tf = tf;
        this.rand = rand;
    }

    public TaxiL2Domain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
        this.rand = this.randomFactory.ingetOrSeedMapped(0, 0);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;

        if (rf == null) {
            rf = new UniformCostRF();
        }
        if (tf == null) {
            tf = new NullTermination();
        }

        domain.addStateClass(PASSENGERL2CLASS, TaxiL2Passenger.class)
                .addStateClass(LOCATIONL2CLASS, TaxiL2Location.class);

        // create model
        TaxiL2Model tmodel = new TaxiL2Model(rand);
        FactoredModel model = new FactoredModel(tmodel, rf, tf);
        domain.setModel(model);

        domain.addActionTypes(
                new PutType(),
                new GetType());

        return domain;
    }


    public static class TaxiL2Model implements FullStateModel {

        protected Random rand;

        public TaxiL2Model(Random rand) {
            this.rand = rand;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            int actionInd = actionInd(a.actionName().split("_")[0]);
            List<StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
            
            if (actionInd == 0) {
                getAction(s, a, transitions);
            } else if (actionInd == 1) {
                putAction(s, a, transitions);
            }
            return transitions;
        }

        @Override
        public State sample(State s, Action a) {
            List<StateTransitionProb> stpList = this.stateTransitions(s, a);
            double roll = rand.nextDouble();
            double curSum = 0.;
        
            for (int i = 0; i < stpList.size(); i++) {
                curSum += stpList.get(i).p;
                if (roll < curSum) {
                    return stpList.get(i).s;
                }
            }
            throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
        }

        private void putAction(State s, Action a, List<StateTransitionProb> transitions) {
            TaxiL2State ns = (TaxiL2State) s.copy();
            String location = ((PutType.PutAction) a).location;

            for(TaxiL2Passenger p : ns.passengers){
            	if(p.inTaxi){
            		TaxiL2Passenger p2 = ns.touchPassenger(p.name());
            		p2.inTaxi = false;
            		p2.currentLocation = location;
            		break;
            	}
            }
            
            transitions.add(new StateTransitionProb(ns, 1.));
        }

        private void getAction(State s, Action a, List<StateTransitionProb> transitions) {
            TaxiL2State ns = (TaxiL2State) s.copy();
            String passengerName = ((GetType.GetAction) a).passenger;
            TaxiL2Passenger p1 = ns.touchPassenger(passengerName);
            boolean flag = true;
            for (TaxiL2Passenger p : ns.passengers) {
                if (p.inTaxi) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                p1.inTaxi = true;
                p1.pickUpOnce = true;
            }
            transitions.add(new StateTransitionProb(ns, 1.));
        }


        protected int actionInd(String name) {
            if (name.equals(ACTION_GET)) {
                return 0;
            } else if (name.equals(ACTION_PUT)) {
                return 1;
            }
            throw new RuntimeException("Unknown action " + name);
        }
    }


    /**
     * Describes the navigate action at level 1 of the AMDP
     */
    public static class GetType implements ActionType {
        @Override
        public String typeName() {
            return ACTION_GET;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new GetAction(strRep);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiL2Passenger> passengers = ((TaxiL2State) s).passengers;
            for (TaxiL2Passenger passenger : passengers) {
                actions.add(new GetAction(passenger.name()));

            }
            return actions;
        }

        public static class GetAction implements Action {

            public String passenger;

            public GetAction(String passenger) {
                this.passenger = passenger;
            }

            @Override
            public String actionName() {
                return ACTION_GET + "_" + passenger;
            }

            @Override
            public Action copy() {
                return new GetAction(passenger);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                GetAction that = (GetAction) o;

                return that.passenger.equals(passenger);

            }

            @Override
            public int hashCode() {
                String str = ACTION_GET + "_" + passenger;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }


    }

    public static class PutType implements ActionType {

    	@Override
        public String typeName() {
            return ACTION_PUT;
        }

        @Override
        public Action associatedAction(String strRep) {
            return new PutAction(strRep.split("_")[0]);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            List<Action> actions = new ArrayList<Action>();
            List<TaxiL2Location> locations = ((TaxiL2State) s).locations;
            for (TaxiL2Location loc : locations) {
            	actions.add(new PutAction(loc.colour));
            }

            return actions;
        }

        public static class PutAction implements Action {

            public String location;

            public PutAction( String location) {
                this.location = location;
            }

            @Override
            public String actionName() {
                return ACTION_PUT + "_" + location;
            }

            @Override
            public Action copy() {
                return new PutAction(location);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                PutAction that = (PutAction) o;

                return  that.location.equals(location);

            }

            @Override
            public int hashCode() {
                String str = ACTION_PUT +  "_" + location;
                return str.hashCode();
            }

            @Override
            public String toString() {
                return this.actionName();
            }
        }
    }
}
