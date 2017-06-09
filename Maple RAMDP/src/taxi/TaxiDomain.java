package taxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Domain;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import taxi.state.TaxiAgent;
import taxi.state.TaxiLocation;
import taxi.state.TaxiMapWall;
import taxi.state.TaxiPassenger;
import taxi.state.TaxiState;


/**
 * Created by ngopalan on 6/14/16.
 */


//fickle works only after passenger has moved one stop
//fickle passenger is sampled randomly from the start state
//taxi is placed randomly at start states
//passenger needs to be picked and dropped before termination
//TODO: add proposition functions

public class TaxiDomain implements DomainGenerator{

    public static final String								VAR_X = "xAtt";
    public static final String								VAR_Y = "yAtt";
    public static final String								VAR_FUEL = "fuelAtt";
    public static final String								VAR_INTAXI = "inTaxiAtt";
    public static final String								VAR_OCCUPIEDTAXI = "occupiedTaxiAtt";
    public static final String								VAR_WALLMIN = "wallMinAtt";
    public static final String								VAR_WALLMAX = "wallMaxAtt";
    public static final String								VAR_WALLOFFSET = "wallOffsetAtt";
    public static final String								VAR_PICKEDUPATLEASTONCE = "pickedUpAtLeastOnce";
    public static final String								VAR_VERTICALWALL = "verticalWall";

    // this is the current location attribute
    public static final String								VAR_LOCATION = "locationAtt";
    public static final String								VAR_GOALLOCATION = "goalLocationAtt";
    public static final String								VAR_ORIGINALSOURCELOCATION = "originalSourceLocationAtt";

    public static final String								VAR_JUSTPICKEDUP = "justPickedupAtt";


    public static final String								TAXICLASS = "taxi";
    public static final String								LOCATIONCLASS = "location";
    public static final String								PASSENGERCLASS = "passenger";
    //    public static final String								HWALLCLASS = "horizontalWall";
    //    public static final String								VWALLCLASS = "verticalWall";
    public static final String								WALLCLASS = "wall";



    public static final String								ACTION_NORTH = "north";
    public static final String								ACTION_SOUTH = "south";
    public static final String								ACTION_EAST = "east";
    public static final String								ACTION_WEST = "west";
    public static final String								ACTION_PICKUP = "pickup";
    public static final String								ACTION_DROPOFF = "dropoff";
    public static final String								ACTION_FILLUP = "fillup";


    public static final String								TAXIATLOCATIONPF = "taxiAt";
    public static final String								PASSENGERATGOALLOCATIONPF = "passengerAtGoal";
    public static final String								TAXIATPASSENGERPF = "taxiAtPassenger";
    public static final String								WALLNORTHPF = "wallNorth";
    public static final String								WALLSOUTHPF = "wallSouth";
    public static final String								WALLEASTPF = "wallEast";
    public static final String								WALLWESTPF = "wallWest";

    public static final String								PASSENGERPICKUPPF = "passengerPickUpPF";
    public static final String								PASSENGERPUTDOWNPF = "passengerPutDownPF";
    public static final String								PASSENGERINTAXI = "passengerInTaxi";
    
    public static final String                              FUELLOCATION = "fuel";



    public static int												maxX = 5;
    public static int												maxY = 5;
    public static int												maxFuel = 12;


    public static final String                              RED = "red";
    public static final String                              GREEN = "green";
    public static final String                              BLUE = "blue";
    public static final String                              YELLOW = "yellow";
    public static final String                              DARKGREY = "darkgrey";
    //    public static final String                              RED = "red";
//    public static final String                              RED = "red";
    public static final String                              MAGENTA = "magenta";
    public static final String                              PINK = "pink";
    public static final String                              ORANGE = "orange";
    public static final String                              CYAN = "cyan";
    public static final String                              FUEL = "fuel";


    /**
     * Matrix specifying the transition dynamics in terms of movement directions. The first index
     * indicates the action direction attempted (ordered north, south, east, west) the second index
     * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
     * The value is the probability of that outcome. The existence of walls does not affect the probability
     * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
     * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
     * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
     */
    protected double[][]								moveTransitionDynamics;
    protected double[][]								fickleLocationDynamics;

    public boolean											includeFuel = true;
    public boolean                                          fickleTaxi = false;
    public double                                           fickleProbability = 0.3;


    protected RandomFactory randomFactory = new RandomFactory();
    protected Random rand;


    protected RewardFunction rf;
    protected TerminalFunction tf;

    public RewardFunction getRf() {
        return rf;
    }

    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    public TerminalFunction getTf() {
        return tf;
    }

    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }
    public double getFickleProbability() {
        return fickleProbability;
    }

    public void setFickleProbability(double fickleProbability) {
        if(fickleProbability <=1. && fickleProbability >=0.) {
            this.fickleProbability = fickleProbability;
        }
        else {
            throw new RuntimeException("Value is not within 0.0 and 1.0 : " + fickleProbability);
        }
    }

    public boolean isFickleTaxi() {
        return fickleTaxi;
    }

    public void setFickleTaxi(boolean fickleTaxi) {
        this.fickleTaxi = fickleTaxi;
    }

    public boolean isIncludeFuel() {
        return includeFuel;
    }

    public void setIncludeFuel(boolean includeFuel) {
        this.includeFuel = includeFuel;
    }

    public RandomFactory getRandomFactory() {
        return randomFactory;
    }

    public void setRandom(Random rand){
        this.rand =rand;
    }

    public void setRandomFactory(RandomFactory randomFactory) {
        this.randomFactory = randomFactory;
    }

    public TaxiDomain(RewardFunction rf, TerminalFunction tf) {
        this.rf = rf;
        this.tf = tf;
        this.setDeterministicTransitionDynamics();
        this.rand = randomFactory.ingetMapped(0);
    }

    private void setDeterministicTransitionDynamics() {
        int directions = 4;
        int na = 4;
        moveTransitionDynamics = new double[na][na];
        for(int i = 0; i < na; i++){
            for(int j = 0; j < na; j++){
                if(i != j){
                    moveTransitionDynamics[i][j] = 0.;
                }
                else{
                    moveTransitionDynamics[i][j] = 1.;
                }
            }
        }
    }

    /**
     * Will set the movement direction probabilities, based on the action chosen and passenger's goal location change probability.
     * The index (0,1,2,3) indicates the direction north,south,east,west, respectively and the matrix is organized by
     * transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     * The index (0,1,...n) indicates the locations the passenger which might be the passenger's destinations, and the matrix is organized
     * by the fickle passengers intial location preference when picked up, to the final location preference, chosen when the taxi
     * has taken one move action location TransitionDynamics[initialGoalLocation][finalGoalLocation].
     * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
     * @param locationTransitionDynamics entries indicate the probability of new goal location(second index) for a passenger's original goal location (first index).
     */
    public void setTransitionDynamics(double [][] transitionDynamics, double[][] locationTransitionDynamics){
        this.moveTransitionDynamics = transitionDynamics.clone();
        this.fickleLocationDynamics = locationTransitionDynamics.clone();
    }

    /**
     * Will set the movement direction probabilities, based on the action chosen and passenger's goal location change probability.
     * The index (0,1,2,3) indicates the direction north,south,east,west, respectively and the matrix is organized by
     * transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     * The index (0,1,...n) indicates the locations the passenger which might be the passenger's destinations, and the matrix is organized
     * by the fickle passengers intial location preference when picked up, to the final location preference, chosen when the taxi
     * has taken one move action location TransitionDynamics[initialGoalLocation][finalGoalLocation].
     */
    public void setTransitionDynamicsLikeFickleTaxiProlem(){
        moveTransitionDynamics = new double[][]{{0.8, 0., 0.1, 0.1},
                {0., 0.8, 0.1, 0.1},
                {0.1, 0.1, 0.8, 0.0},
                {0.1, 0.1, 0., 0.8}};

        fickleLocationDynamics = new double[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(i != j){
                    fickleLocationDynamics[i][j] = 0.075;
                }
                else{
                    fickleLocationDynamics[i][j] = 0.775;
                }
            }
        }
    }

    public void setTransitionNondetirministicDynamics(){
        moveTransitionDynamics = new double[][]{{0.8, 0., 0.1, 0.1},
                {0., 0.8, 0.1, 0.1},
                {0.1, 0.1, 0.8, 0.0},
                {0.1, 0.1, 0., 0.8}};
    }

    /**
     * Will set the movement direction probabilities based on the action chosen. The index (0,1,2,3) indicates the
     * direction north,south,east,west, respectively and the matrix is organized by transitionDynamics[selectedDirection][actualDirection].
     * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
     *This setup is for when the taxi is not fickle
     * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
     */
    public void setTransitionDynamics(double [][] transitionDynamics){
        this.moveTransitionDynamics = transitionDynamics.clone();
    }


    @Override
    public OOSADomain generateDomain() {
        // need a boolean for the fickle nature and another for the transition dynamics
//        Random rand = randomFactory.ingetMapped(0);

        OOSADomain domain = new OOSADomain();

        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;

        if(rf == null){
            rf = new UniformCostRF();
        }
        if(tf == null){
            tf = new NullTermination();
        }

        domain.addStateClass(TAXICLASS, TaxiAgent.class).addStateClass(LOCATIONCLASS, TaxiLocation.class)
                .addStateClass(PASSENGERCLASS, TaxiPassenger.class);

        if(fickleTaxi){
            TaxiModel smodel = new TaxiModel(rand, moveTransitionDynamics,
                    fickleLocationDynamics, fickleTaxi, fickleProbability, includeFuel );
            FactoredModel model = new FactoredModel(smodel, rf, tf);
            domain.setModel(model);
        }
        else{
            TaxiModel smodel = new TaxiModel(rand, moveTransitionDynamics, includeFuel );
            FactoredModel model = new FactoredModel(smodel, rf, tf);
            domain.setModel(model);
        }


        if(includeFuel){
            domain.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST),
                    new UniversalActionType(ACTION_DROPOFF),
                    new UniversalActionType(ACTION_FILLUP),
                    new UniversalActionType(ACTION_PICKUP));
        }
        else{
            domain.addActionTypes(
                    new UniversalActionType(ACTION_NORTH),
                    new UniversalActionType(ACTION_SOUTH),
                    new UniversalActionType(ACTION_EAST),
                    new UniversalActionType(ACTION_WEST),
                    new UniversalActionType(ACTION_DROPOFF),
                    new UniversalActionType(ACTION_PICKUP));
        }

        OODomain.Helper.addPfsToDomain(domain, this.generatePfs(domain));



        return domain;
    }

    private List<PropositionalFunction> generatePfs(OOSADomain domain) {
        List<PropositionalFunction> pfs = new ArrayList<PropositionalFunction>();
        pfs.add(new PF_PassengerAtGoalLoc(PASSENGERATGOALLOCATIONPF, domain,new String[]{PASSENGERCLASS}));
        pfs.add(new PF_PickUp(PASSENGERPICKUPPF,domain,new String[]{}));
        pfs.add(new PF_PutDown(PASSENGERPUTDOWNPF,new String[]{}));
        pfs.add(new PF_TaxiAtLoc(TAXIATLOCATIONPF,new String[]{LOCATIONCLASS}));
        pfs.add(new PF_PassengerInTaxi(PASSENGERINTAXI, new String[]{PASSENGERCLASS}));
        return pfs;
    }


    public static class TaxiModel implements FullStateModel{
        protected Random rand;
        protected double[][] movementTransitionDynamics;
        protected double[][] locationTransitionDynamics;
        protected boolean fickleTaxi;
        protected double fickleProbability = 0.;
        protected boolean includeFuel;

        public TaxiModel(Random rand, double[][] movementTransitionDynamics, boolean includeFuel) {
            this.rand = rand;
            this.movementTransitionDynamics = movementTransitionDynamics;
            this.fickleTaxi = false;
            this.includeFuel = includeFuel;
        }

        public TaxiModel(Random rand, double[][] movementTransitionDynamics, double[][] locationTransitionDynamics,
                         boolean fickleTaxi, double fickleProbability, boolean includeFuel) {
            this.rand = rand;
            this.movementTransitionDynamics = movementTransitionDynamics;
            this.fickleTaxi = fickleTaxi;
            this.fickleProbability = fickleProbability;
            if(fickleTaxi && locationTransitionDynamics==null){
                throw new RuntimeException("transition dynamics for switching locations are null but the passenger is fickle.");
            }
            this.includeFuel = includeFuel;
            this.locationTransitionDynamics = locationTransitionDynamics;
        }

        @Override
        public List<StateTransitionProb> stateTransitions(State s, Action a) {
            int actionInd = actionInd(a.actionName());

            List <StateTransitionProb> transitions = new ArrayList<StateTransitionProb>();
            if(actionInd<4){
                double [] directionProbs = movementTransitionDynamics[actionInd(a.actionName())];

                for(int i = 0; i < directionProbs.length; i++) {
                    double p = directionProbs[i];
                    if (p == 0.) {
                        continue; //cannot transition in this direction
                    }
                    State ns = s.copy();
                    int[] dcomps = movementDirectionFromIndex(i);
                    ns = move(ns, dcomps[0], dcomps[1]);


                    if (fickleTaxi) {
                        boolean passengersNotChangingDestinationFlag = true;

                        List<ObjectInstance> passengers = ((TaxiState)ns).objectsOfClass(PASSENGERCLASS);
                        for(ObjectInstance pass : passengers){
                            if(!((TaxiPassenger)pass).justPickedUp){
                                continue;
                            }
                            else{
                                // if passenger has not moved then continue
//                                System.out.println("passenger was just picked up!");
                                if(!passengerMoved(s,ns, (TaxiPassenger)pass)){
                                    continue;
                                }
                                passengersNotChangingDestinationFlag  = false;
                                // ns has the move information nns will have the new changed destination
                                // get current location index
                                int locationIndex = ((TaxiState) ns).locationIndWithColour(((TaxiPassenger)pass).goalLocation);
                                if(locationIndex==-1){
                                    throw new RuntimeException("Unknown location as passenger goal: " + ((TaxiPassenger)pass).goalLocation);
                                }
                                double[] locationChangeProbabilities =locationTransitionDynamics[locationIndex];
                                List<ObjectInstance> locations = ((TaxiState) ns).objectsOfClass(LOCATIONCLASS);

                                for (int j = 0; j <locationChangeProbabilities.length;j++){
                                    State nns = ns.copy();
                                    TaxiPassenger passN = ((TaxiState)nns).touchPassenger(pass.name());
                                    String newGoal = new String(((TaxiLocation) locations.get(j)).colour);
                                    passN.goalLocation = newGoal;
                                    passN.justPickedUp=false;
                                    transitions.add(new StateTransitionProb(nns,p*locationChangeProbabilities[j]));
                                }


                            }

                        }
                        if(passengersNotChangingDestinationFlag){
                            transitions.add(new StateTransitionProb(ns,p));

                        }
                    }
                    else{
                        transitions.add(new StateTransitionProb(ns,p));
                    }

                }

                return transitions;
            }
            if(actionInd==4){
                //pick
                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = ns.touchTaxi();
                int tx = taxi.x;
                int ty = taxi.y;
                boolean taxiOccupied = taxi.taxiOccupied;

                if(!taxiOccupied){
                    List<ObjectInstance> passengers = ((TaxiState)s).objectsOfClass(PASSENGERCLASS);
                    for(ObjectInstance p : passengers){
                        int px = ((TaxiPassenger)p).x;
                        int py = ((TaxiPassenger)p).y;

                        if(tx == px && ty == py ){
                            int passID = ns.passengerInd(((TaxiPassenger)p).name());
                            TaxiPassenger np = ns.touchPassenger(passID);
                            np.inTaxi = true;
                            if(fickleTaxi) {
                                np.justPickedUp = true;
                            }
                            taxi.taxiOccupied = true;
                            np.pickedUpAtLeastOnce = true;
                            break;
                        }
                    }
                }
                transitions.add(new StateTransitionProb(ns, 1.0));
                return transitions;
            }

            if(actionInd==5){
                //drop
                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = ns.touchTaxi();
                boolean taxiOccupied = taxi.taxiOccupied;

                if(taxiOccupied){
                    List<ObjectInstance> passengers = ns.objectsOfClass(PASSENGERCLASS);
                    List<ObjectInstance> locationList = ns.objectsOfClass(LOCATIONCLASS);
                    for(ObjectInstance p : passengers){
                        boolean in = ((TaxiPassenger)p).inTaxi;
                        if(in){
                            for(ObjectInstance l :locationList){
                                if(((TaxiLocation) l).x == ((TaxiPassenger) p).x
                                        && ((TaxiLocation) l).y == ((TaxiPassenger) p).y){
                                    int passID = ns.passengerInd(((TaxiPassenger) p).name());
                                    TaxiPassenger np = ns.touchPassenger(passID);
                                    np.inTaxi = false;
                                    taxi.taxiOccupied = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                transitions.add(new StateTransitionProb(ns, 1.0));
                return transitions;

            }

            if(actionInd==6){
                // fillup
                if(!includeFuel){
                    transitions.add(new StateTransitionProb(s,1.));
                    return transitions;
                }
                TaxiState ns = (TaxiState)s.copy();
                TaxiAgent taxi = (TaxiAgent)((TaxiState)s).objectsOfClass(TAXICLASS).get(0);
                int tx = taxi.x;
                int ty = taxi.y;

                List<ObjectInstance> locations = ((TaxiState)s).objectsOfClass(LOCATIONCLASS);
                for(ObjectInstance l : locations){
                    if(((TaxiLocation)l).colour.equals(FUELLOCATION)){
                        int lx = ((TaxiLocation)l).x;
                        int ly = ((TaxiLocation)l).y;
                        if(tx == lx && ty == ly){
                            TaxiAgent ntaxi = ns.touchTaxi();
                            ntaxi.fuel = maxFuel;
                        }
                    }
                }
                transitions.add(new StateTransitionProb(ns,1.0));
                return transitions;

            }

            return transitions;
        }

        private boolean passengerMoved(State s, State ns, TaxiPassenger pass) {
            TaxiPassenger pOld = (TaxiPassenger)((TaxiState)s).object(pass.name());
            TaxiPassenger pNew = (TaxiPassenger)((TaxiState)ns).object(pass.name());
            double distance = Math.abs(pOld.x - pNew.x) +Math.abs(pOld.y - pNew.y);
            return distance>0;

        }

        @Override
        public State sample(State s, Action a) {
            List<StateTransitionProb> stpList = this.stateTransitions(s,a);
            double roll = rand.nextDouble();
            double curSum = 0.;
            int dir = 0;
            for(int i = 0; i < stpList.size(); i++){
                curSum += stpList.get(i).p;
                if(roll < curSum){
                    return stpList.get(i).s;
                }
            }
            throw new RuntimeException("Probabilities don't sum to 1.0: " + curSum);
        }


        /**
         * Attempts to move the agent into the given position, taking into account walls and blocks
         * @param sIn the current state
         * @param dx the attempted new X position of the agent
         * @param dy the attempted new Y position of the agent
         * @return input state s, after modification
         */
        protected State move(State sIn, int dx, int dy){
            Random rand = RandomFactory.getMapped(0);

            TaxiState ts = (TaxiState)sIn;

            int tx = ts.taxi.x;
            int ty = ts.taxi.y;

            int nx = tx+dx;
            int ny = ty+dy;

            //using fuel?
            TaxiAgent taxi = ts.touchTaxi();
            if(includeFuel){
                int fuel = taxi.fuel;
                if(fuel == 0){
                    //no movement possible
                    return ts;
                }
                taxi.fuel-=1;
            }

            //hit wall, so do not change position
//        if(nx < 0 || nx >= map.length || ny < 0 || ny >= map[0].length || map[nx][ny] == 1 ||
//                (dx > 0 && (map[ax][ay] == 3 || map[ax][ay] == 4)) || (dx < 0 && (map[nx][ny] == 3 || map[nx][ny] == 4)) ||
//                (dy > 0 && (map[ax][ay] == 2 || map[ax][ay] == 4)) || (dy < 0 && (map[nx][ny] == 2 || map[nx][ny] == 4)) ){
//            nx = ax;
//            ny = ay;
//        }



            //check for all wall boundings

            if(dx > 0){
                List<ObjectInstance> vwalls = ts.objectsOfClass(WALLCLASS);
                for(ObjectInstance wall : vwalls){
                    if(wallEast(tx, ty, (TaxiMapWall) wall) && ((TaxiMapWall)wall).verticalWall){
                        nx = tx;
                        break;
                    }
                }
            }
            else if(dx < 0){
                List<ObjectInstance> vwalls = ts.objectsOfClass(WALLCLASS);
                for(ObjectInstance wall : vwalls){
                    if(wallWest(tx, ty, (TaxiMapWall)wall) && ((TaxiMapWall)wall).verticalWall){
                        nx = tx;
                        break;
                    }
                }
            }
            else if(dy > 0){
                List<ObjectInstance> hwalls = ts.objectsOfClass(WALLCLASS);
                for(ObjectInstance wall : hwalls){
                    if(wallNorth(tx, ty, (TaxiMapWall)wall) && !((TaxiMapWall)wall).verticalWall){
                        ny = ty;
                        break;
                    }
                }
            }
            else if(dy < 0){
                List<ObjectInstance> hwalls = ts.objectsOfClass(WALLCLASS);
                for(ObjectInstance wall : hwalls){
                    if(wallSouth(tx, ty, (TaxiMapWall)wall) && !((TaxiMapWall)wall).verticalWall){
                        ny = ty;
                        break;
                    }
                }
            }

            taxi.x = nx;
            taxi.y = ny;


            List<ObjectInstance> passengers = ts.objectsOfClass(PASSENGERCLASS);
            for(ObjectInstance p : passengers){
                boolean inTaxi = ((TaxiPassenger)p).inTaxi;
//                    p.getIntValForAttribute(INTAXIATT);
                if(inTaxi){
                    TaxiPassenger pN = ts.touchPassenger(p.name());
                    pN.x = nx;
                    pN.y = ny;
                }
            }

            return ts;
        }


//        @Override
//        public List<StateTransitionProb> stateTransitions(State s, Action a) {
//            return null;
//        }
    }


    public class PF_PassengerInTaxi extends PropositionalFunction {

        public PF_PassengerInTaxi(String name, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            TaxiState ns = ((TaxiState)s).copy();
            TaxiAgent taxi = ns.taxi;
            int tx = taxi.x, ty = taxi.y;
            boolean taxiOccupied = taxi.taxiOccupied;

            boolean returnValue = false;
            TaxiPassenger passenger = ns.touchPassenger(params[0]);
            int px = passenger.x, py = passenger.y;
            boolean inTaxi = passenger.inTaxi;
            if(tx == px && ty == py && inTaxi && taxiOccupied ){
                returnValue = true;
            }

            return returnValue;
        }
    }

    //propositional function for taxi at location for navigate
    public class PF_TaxiAtLoc extends PropositionalFunction {
        public PF_TaxiAtLoc(String name, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
        	TaxiState state = (TaxiState) s;
        	String locationName = params[0];
        	TaxiLocation location = (TaxiLocation) state.object(locationName);
        	int taxiX = state.taxi.x;
        	int taxiY = state.taxi.y;
    		if (taxiX == location.x && taxiY == location.y) {
    			return true;
    		}
    		return false;
//            TaxiState ns = ((TaxiState)s).copy();
//            TaxiAgent taxi = ns.taxi;
//            ObjectInstance o = s.getFirstObjectOfClass(TAXICLASS);
//            int xt = taxi.x;
//            int yt = taxi.y;
//            // params here are the name of a location like Location 1
//            boolean returnValue = false;
//            int i = ns.locationIndWithColour(params[0]);
//            TaxiLocation location = ns.touchLocation(i);//TaxiLocation)((TaxiState)s).object(params[0]);
//            int xl = location.x;
//            int yl = location.y;
//            if(xt==xl && yt==yl ){
//                returnValue = true;
//            }
//
//            return returnValue;
        }

    }


    public class PF_PassengerAtGoalLoc extends PropositionalFunction{

        public PF_PassengerAtGoalLoc(String name, Domain domain, String [] params){
            super(name, params);
        }


        @Override
        public boolean isTrue(OOState s, String... params) {
            TaxiPassenger p = (TaxiPassenger)s.object(params[0]);
            int xp = p.x;
            int yp = p.y;
            boolean inTaxi = p.inTaxi;
            String goalLocation = p.goalLocation;
            // params here are the name of a location like Location 1

            boolean returnValue = false;
            List<ObjectInstance> locations = s.objectsOfClass(LOCATIONCLASS);
            for(ObjectInstance location : locations){
                if(((TaxiLocation)location).colour.equals(goalLocation)){
                    int xl = ((TaxiLocation)location).x;
                    int yl = ((TaxiLocation)location).y;
                    if(xp==xl && yp==yl && !inTaxi ){
                        returnValue = true;
                    }
                    break;
                }

            }


            return returnValue;
        }
    }

    //propositional function for pick up
    public class PF_PickUp extends PropositionalFunction{



        public PF_PickUp(String name, Domain domain, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            TaxiAgent taxiAgent = (TaxiAgent)s.objectsOfClass(TAXICLASS).get(0);
            int xt = taxiAgent.x;
            int yt = taxiAgent.y;
            boolean taxiOccupied = taxiAgent.taxiOccupied;
            // params here are the location colour - red, green, blue, yellow, magenta

            boolean returnValue = false;
            List<ObjectInstance> passengers = s.objectsOfClass(PASSENGERCLASS);
            for(int i=0;i<passengers.size();i++){
                int xp = ((TaxiPassenger)passengers.get(i)).x;
                int yp = ((TaxiPassenger)passengers.get(i)).y;
                boolean inTaxi = ((TaxiPassenger) passengers.get(i)).inTaxi;
                if(xt==xp && yt==yp && inTaxi && taxiOccupied){
                    returnValue = true;
                    break;
                }
            }
            return returnValue;
        }
    }

    //propositional function for put down

    public class PF_PutDown extends PropositionalFunction{


        public PF_PutDown(String name, String [] params){
            super(name, params);
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            TaxiAgent taxiAgent = (TaxiAgent)s.objectsOfClass(TAXICLASS).get(0);
            int xt = taxiAgent.x;
            int yt = taxiAgent.y;
            boolean taxiOccupied = taxiAgent.taxiOccupied;
            // params here are the location colour - red, green, blue, yellow, magenta

            boolean returnValue = false;
            List<ObjectInstance> passengers = s.objectsOfClass(PASSENGERCLASS);
            for(int i=0;i<passengers.size();i++){
                int xp = ((TaxiPassenger)passengers.get(i)).x;
                int yp = ((TaxiPassenger)passengers.get(i)).y;
                boolean inTaxi = ((TaxiPassenger)passengers.get(i)).inTaxi;
                if(xt==xp && yt==yp && !inTaxi && !taxiOccupied){
                    returnValue = true;
                    break;
                }
            }
            return returnValue;
        }
    }



    /**
     * Returns the change in x and y position for a given direction number.
     * @param i the direction number (0,1,2,3 indicates north,south,east,west, respectively)
     * @return the change in direction for x and y; the first index of the returned double is change in x, the second index is change in y.
     */
    protected static int [] movementDirectionFromIndex(int i){

        int [] result = null;

        switch (i) {
            case 0:
                result = new int[]{0,1};
                break;

            case 1:
                result = new int[]{0,-1};
                break;

            case 2:
                result = new int[]{1,0};
                break;

            case 3:
                result = new int[]{-1,0};
                break;

            default:
                break;
        }

        return result;
    }



    protected static int actionInd(String name){
        if(name.equals(ACTION_NORTH)){
            return 0;
        }
        else if(name.equals(ACTION_SOUTH)){
            return 1;
        }
        else if(name.equals(ACTION_EAST)){
            return 2;
        }
        else if(name.equals(ACTION_WEST)){
            return 3;
        }
        else if(name.equals(ACTION_PICKUP)){
            return 4;
        }
        else if(name.equals(ACTION_DROPOFF)){
            return 5;
        }
        else if(name.equals(ACTION_FILLUP)){
            return 6;
        }
        throw new RuntimeException("Unknown action " + name);
    }


    protected static boolean wallEast(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == tx+1){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return ty >= wallmin && ty < wallmax;
        }
        return false;
    }

    protected static boolean wallWest(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == tx){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return ty >= wallmin && ty < wallmax;
        }
        return false;
    }


    protected static boolean wallNorth(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == ty+1){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return tx >= wallmin && tx < wallmax;
        }
        return false;
    }

    protected static boolean wallSouth(int tx, int ty, TaxiMapWall wall){
        int wallo = wall.wallOffset;
        if(wallo == ty){
            int wallmin = wall.wallMin;
            int wallmax = wall.wallMax;
            return tx >= wallmin && tx < wallmax;
        }
        return false;
    }

    public static TaxiState getTinyClassicState( boolean usesFuel){
    	
        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,0);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0, 0, 1, RED, BLUE);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,RED);
        TaxiLocation l1 = new TaxiLocation(0, 1,LOCATIONCLASS+1,BLUE);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);

        taxiPassengers.add(p1);

        TaxiMapWall wall0 = new TaxiMapWall(WALLCLASS+0,0, 5, 0, false);
        TaxiMapWall wall1 = new TaxiMapWall(WALLCLASS+1,0, 5, 5, false);
        TaxiMapWall wall2 = new TaxiMapWall(WALLCLASS+2,0, 5, 0, true);
        TaxiMapWall wall3 = new TaxiMapWall(WALLCLASS+3,0, 5, 5, true);
        TaxiMapWall wall4 = new TaxiMapWall(WALLCLASS+4,0, 5, 1, true);
        TaxiMapWall wall5 = new TaxiMapWall(WALLCLASS+5,3, 5, 2, true);
        TaxiMapWall wall6 = new TaxiMapWall(WALLCLASS+6,0, 2, 3, true);
        TaxiMapWall wall7 = new TaxiMapWall(WALLCLASS+7,0, 2, 2, false);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(wall0);
        walls.add(wall1);
        walls.add(wall2);
        walls.add(wall3);
        walls.add(wall4);
        walls.add(wall5);
        walls.add(wall6);
        walls.add(wall7);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;
    }

    public static TaxiState getClassicState(boolean usesFuel){

        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,3);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,3, 0, RED, BLUE);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        taxiPassengers.add(p1);

        TaxiMapWall wall0 = new TaxiMapWall(WALLCLASS+0,0, 5, 0, false);
        TaxiMapWall wall1 = new TaxiMapWall(WALLCLASS+1,0, 5, 5, false);
        TaxiMapWall wall2 = new TaxiMapWall(WALLCLASS+2,0, 5, 0, true);
        TaxiMapWall wall3 = new TaxiMapWall(WALLCLASS+3,0, 5, 5, true);
        TaxiMapWall wall4 = new TaxiMapWall(WALLCLASS+4,0, 2, 1, true);
        TaxiMapWall wall5 = new TaxiMapWall(WALLCLASS+5,3, 5, 2, true);
        TaxiMapWall wall6 = new TaxiMapWall(WALLCLASS+6,0, 2, 3, true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(wall0);
        walls.add(wall1);
        walls.add(wall2);
        walls.add(wall3);
        walls.add(wall4);
        walls.add(wall5);
        walls.add(wall6);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;

    }

    public static TaxiState getComplexState(boolean usesFuel){
        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,3);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,0, 0, BLUE, YELLOW);
        TaxiPassenger p2 = new TaxiPassenger(PASSENGERCLASS+1,3, 0, GREEN, BLUE);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        taxiPassengers.add(p2);
        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(WALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(WALLCLASS+1,0, 5, 5,false);
        TaxiMapWall v1 = new TaxiMapWall(WALLCLASS+2,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(WALLCLASS+3,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(WALLCLASS+4,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(WALLCLASS+5,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(WALLCLASS+6,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;
    }

    public static TaxiState getRandomClassicState(Random rand, boolean usesFuel){
        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0, rand.nextInt(maxX),rand.nextInt(maxY));

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        TaxiLocation tempStartLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));
        TaxiLocation tempGoalLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,tempStartLocation.x, tempStartLocation.y, tempGoalLocation.colour, tempStartLocation.colour);
        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(WALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(WALLCLASS+1,0, 5, 5,false);
        TaxiMapWall v1 = new TaxiMapWall(WALLCLASS+2,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(WALLCLASS+3,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(WALLCLASS+4,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(WALLCLASS+5,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(WALLCLASS+6,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;

    }

    public static TaxiState getStartStateFromTaxiPosition(int TaxiX, int TaxiY, Random rand, boolean usesFuel){
        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0, TaxiX, TaxiY);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 4,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(3, 0,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(4, 4,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        TaxiLocation tempStartLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));
        TaxiLocation tempGoalLocation = taxiLocations.get(rand.nextInt(taxiLocations.size()));

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0,tempStartLocation.x, tempStartLocation.y, tempGoalLocation.colour, tempStartLocation.colour);
        taxiPassengers.add(p1);

        TaxiMapWall h1 = new TaxiMapWall(WALLCLASS+0,0, 5, 0,false);
        TaxiMapWall h2 = new TaxiMapWall(WALLCLASS+1,0, 5, 5,false);
        TaxiMapWall v1 = new TaxiMapWall(WALLCLASS+2,0, 5, 0,true);
        TaxiMapWall v2 = new TaxiMapWall(WALLCLASS+3,0, 5, 5,true);
        TaxiMapWall v3 = new TaxiMapWall(WALLCLASS+4,0, 2, 1,true);
        TaxiMapWall v4 = new TaxiMapWall(WALLCLASS+5,3, 5, 2,true);
        TaxiMapWall v5 = new TaxiMapWall(WALLCLASS+6,0, 2, 3,true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(h1);
        walls.add(h2);
        walls.add(v1);
        walls.add(v2);
        walls.add(v3);
        walls.add(v4);
        walls.add(v5);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;
    }
    
    public static TaxiState getSmallClassicState(boolean usesFuel){
        TaxiAgent taxiAgent = new TaxiAgent(TAXICLASS+0,0,0);

        TaxiPassenger p1 = new TaxiPassenger(PASSENGERCLASS+0, 0, 1, RED, BLUE);

        TaxiLocation l0 = new TaxiLocation(0, 0,LOCATIONCLASS+0,YELLOW);
        TaxiLocation l1 = new TaxiLocation(0, 2,LOCATIONCLASS+1,RED);
        TaxiLocation l2 = new TaxiLocation(0, 1,LOCATIONCLASS+2,BLUE);
        TaxiLocation l3 = new TaxiLocation(0, 3,LOCATIONCLASS+3,GREEN);

        List<TaxiLocation> taxiLocations = new ArrayList<TaxiLocation>();
        List<TaxiPassenger> taxiPassengers= new ArrayList<TaxiPassenger>();

        if(usesFuel){
            TaxiLocation lFuel = new TaxiLocation(2,1,LOCATIONCLASS+4,FUEL);
            taxiLocations.add(lFuel);
        }
        taxiLocations.add(l0);
        taxiLocations.add(l1);
        taxiLocations.add(l2);
        taxiLocations.add(l3);

        taxiPassengers.add(p1);

        TaxiMapWall wall0 = new TaxiMapWall(WALLCLASS+0,0, 5, 0, false);
        TaxiMapWall wall1 = new TaxiMapWall(WALLCLASS+1,0, 5, 5, false);
        TaxiMapWall wall2 = new TaxiMapWall(WALLCLASS+2,0, 5, 0, true);
        TaxiMapWall wall3 = new TaxiMapWall(WALLCLASS+3,0, 5, 5, true);
        TaxiMapWall wall4 = new TaxiMapWall(WALLCLASS+4,0, 5, 1, true);
        TaxiMapWall wall5 = new TaxiMapWall(WALLCLASS+5,3, 5, 2, true);
        TaxiMapWall wall6 = new TaxiMapWall(WALLCLASS+6,0, 2, 3, true);

        List<TaxiMapWall> walls = new ArrayList<TaxiMapWall>();
        walls.add(wall0);
        walls.add(wall1);
        walls.add(wall2);
        walls.add(wall3);
        walls.add(wall4);
        walls.add(wall5);
        walls.add(wall6);

        TaxiState s = new TaxiState(walls,taxiPassengers,taxiLocations,taxiAgent);

        return s;
    }
}