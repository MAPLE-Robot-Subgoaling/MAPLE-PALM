package edu.umbc.cs.maple.taxi;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.umbc.cs.maple.taxi.state.TaxiAgent;
import edu.umbc.cs.maple.taxi.state.TaxiLocation;
import edu.umbc.cs.maple.taxi.state.TaxiPassenger;
import edu.umbc.cs.maple.taxi.state.TaxiWall;
import edu.umbc.cs.maple.taxi.stategenerator.TaxiStateFactory;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.taxi.TaxiConstants.*;

public class Taxi implements DomainGenerator{

    //parameters dictating probabilities of the model
    private RewardFunction rf;
    private TerminalFunction tf;
    private boolean fickle;
    private double fickleProbability;
    private double[][] moveDynamics;
    private double correctMoveProbability;

    public double getCorrectMoveProb(){
        return correctMoveProbability;
    }
    public void setCorrectMoveProb(double correctMoveProbability){
        this.correctMoveProbability = correctMoveProbability;
        setMoveDynamics(correctMoveProbability);

    }


    public boolean getFickle() {
        return fickle;
    }

    public void setFickle(boolean fickle) {
        this.fickle = fickle;
    }

    public double getFickleProbability() {
        return fickleProbability;
    }

    public void setFickleProbability(double fickleProbability) {
        this.fickleProbability = fickleProbability;
    }


    /**
     * create a taxi domain generator
     * @param r rewardTotal function
     * @param t terminal function
     * @param fickle whether the domain is fickle
     * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
     * @param correctMoveprob transitionProbability the taxi will go in the correct direction they select
     */
    public Taxi(RewardFunction r, TerminalFunction t, boolean fickle,
            double fickleprob, double correctMoveprob) {
        rf = r;
        tf = t;
        this.fickle = fickle;
        this.fickleProbability = fickleprob;
        setMoveDynamics(correctMoveprob);
    }

    /**
     * create a taxi domain generator
     * @param fickle whether the domain is fickle
     * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
     * @param correctMoveprob transitionProbability the taxi will go in the correct direction they select
     */
    public Taxi(boolean fickle, double fickleprob, double correctMoveprob) {
        this.fickle = fickle;
        this.fickleProbability = fickleprob;
        setMoveDynamics(correctMoveprob);
        this.rf = new TaxiRewardFunction();
        this.tf = new TaxiTerminalFunction();
    }

    /**
     * create a taxi domain generator
     * @param fickle whether the domain is fickle
     * @param fickleprob transitionProbability the passenger that is just picked up will change their goal
     * @param movement a array saying the transitionProbability of execution each action (2nd index) given
     * the selected action (1rt action)
     */
    public Taxi(boolean fickle, double fickleprob, double[][] movement) {
        this.fickle = fickle;
        this.fickleProbability = fickleprob;
        this.moveDynamics = movement;
        this.rf = new TaxiRewardFunction();
        this.tf = new TaxiTerminalFunction();
    }

    /**
     * creates a non fickle deterministic taxi domain generator
     */
    public Taxi() {
        this(false, 0, 1);
    }

    /**
     * sets the movement array so the right direction will be taken with
     * the given transitionProbability and the perpendicular action the rest of the time
     * @param correctProb the transitionProbability that the correct action is taken
     */
    private void setMoveDynamics(double correctProb){
        moveDynamics = new double[NUM_MOVE_ACTIONS][NUM_MOVE_ACTIONS];

        for(int choose = 0; choose < NUM_MOVE_ACTIONS; choose++){
            for(int outcome = 0; outcome < NUM_MOVE_ACTIONS; outcome++){
                if(choose == outcome){
                    moveDynamics[choose][outcome] = correctProb;
                }
                // the two directions which are one away get the rest of prob
                else if(Math.abs(choose - outcome) % 2 == 1){
                    moveDynamics[choose][outcome] = (1 - correctProb) / 2;
                }else{
                    moveDynamics[choose][outcome] = 0;
                }
            }
        }
    }


    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_TAXI, TaxiAgent.class).addStateClass(CLASS_PASSENGER, TaxiPassenger.class)
                .addStateClass(CLASS_LOCATION, TaxiLocation.class).addStateClass(CLASS_WALL, TaxiWall.class);

        TaxiModel model = new TaxiModel(moveDynamics, fickle, fickleProbability);
        FactoredModel taxiModel = new FactoredModel(model, rf, tf);
        domain.setModel(taxiModel);

        domain.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST),
                new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_PASSENGER}),
                new PickupActionType(ACTION_PICKUP, new String[]{CLASS_PASSENGER}));

        return domain;
    }

    public OOSADomain generateNavigateDomain(){
        OOSADomain d = generateDomain();
        d.clearActionTypes();
        d.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST)
                );
        return d;
    }

    public static void main(String[] args) {

        Taxi taxiBuild = new Taxi();
        OOSADomain domain = taxiBuild.generateDomain();

        HashableStateFactory hs = new SimpleHashableStateFactory();

        State s = TaxiStateFactory.createClassicState();
        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);

        List<Episode> eps = new ArrayList<Episode>();
        QLearning qagent = new QLearning(domain, 0.95, hs, 0, 0.1); //gamma = 0.99 to match the PALM parameters

        //initialize the min and max
        //int maxNumActions = 0;
        //int minNumActions = 5000;
        List <Integer> numberOfActions = new ArrayList <Integer>(1000);

        for(int i = 0; i < 1000; i++){
            Episode e = qagent.runLearningEpisode(env, 5000);
            eps.add(e);

            //System.out.println("e.numActions() is" + ": "+ e.numActions());
            //to see how the number of actions changes depending on the map dimensions and additions
            System.out.println("Number of actions in episode:" + i + ": "+ e.numActions());

            //array list for obtaining optimum numbers of total actions
            numberOfActions.add(e.numActions());

            if (e.actionSequence.contains(ACTION_PUTDOWN)){
                System.out.println("This episode contains a putdown action.");
            }
            /*
            //get the maximum number of actions across all episodes
            if (e.numActions() > maxNumActions){
                maxNumActions = e.numActions();
            }
            //get the minimum number of actions across all episodes
            if (e.numActions() < minNumActions){
                minNumActions = e.numActions();
            }
            */
            env.resetEnvironment();
        }

        //method for getting the min and max number of actions
        //int maxActions = numberOfActions.get(0);
        int minActions = numberOfActions.get(0);
        for (int j = 0; j < numberOfActions.size(); j++){
            //get the maximum number of actions across all episodes
            //if (numberOfActions.get(j) > maxActions){
                //maxActions = numberOfActions.get(j);
            //}
            //get the minimum number of actions across all episodes
            if (numberOfActions.get(j) < minActions){
                minActions = numberOfActions.get(j);
            }


            //show the min and max number of actions
            /**
             * one issue is that the minimum and maximum determinations do not account for failure or proper termination conditions
             **/
            //System.out.println("Minimum number of actions taken as of episode" + j + ": "+ minActions);
            //System.out.println("Maximum number of actions taken as of episode" + j + ": "+ maxActions);
        }


        EpisodeSequenceVisualizer v = new EpisodeSequenceVisualizer(TaxiVisualizer.getVisualizer(20, 20),
                domain, eps);
        v.setDefaultCloseOperation(v.EXIT_ON_CLOSE);
        v.initGUI();
    }

}
