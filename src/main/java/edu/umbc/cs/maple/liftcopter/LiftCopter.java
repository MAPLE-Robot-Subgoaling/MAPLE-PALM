package edu.umbc.cs.maple.liftcopter;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterAgent;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterCargo;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterLocation;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterWall;
import edu.umbc.cs.maple.liftcopter.stategenerator.LiftCopterStateFactory;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.liftcopter.LiftCopterConstants.*;


public class LiftCopter extends OOSADomainGenerator {

    private double correctMoveprob;
    private RewardFunction rf;
    private TerminalFunction tf;
    private double[][] moveDynamics;
    protected List<Double> thrustValues = new ArrayList();
    protected List<Double> directionValues = new ArrayList();
    /**
     * create a liftcopter domain generator
     *
     * @param r               rewardTotal function
     * @param t               terminal function
     * @param correctMoveprob transitionProbability the liftcopter will go in the correct direction they select
     */
    public LiftCopter(RewardFunction r, TerminalFunction t, double correctMoveprob) {
        rf = r;
        tf = t;
        this.correctMoveprob = correctMoveprob;
        setMoveDynamics(correctMoveprob);
    }

    /**
     * create a liftcopter domain generator
     *
     * @param correctMoveprob transitionProbability the liftcopter will go in the correct direction they select
     */
    public LiftCopter(double correctMoveprob) {
        this.correctMoveprob = correctMoveprob;
        setMoveDynamics(correctMoveprob);
        this.rf = new LiftCopterRewardFunction();
        this.tf = new LiftCopterTerminalFunction();
    }

    /**
     * create a liftcopter domain generator
     *
     * @param movement a array saying the transitionProbability of execution each action (2nd index) given
     *                 the selected action (1rt action)
     */
    public LiftCopter(double[][] movement) {
        this.moveDynamics = movement;
        this.rf = new LiftCopterRewardFunction();
        this.tf = new LiftCopterTerminalFunction();
    }

    /**
     * creates a non fickle deterministic copter domain generator
     */
    public LiftCopter() {
        this(1);
    }

    private void setMoveDynamics(double correctProb) {
        moveDynamics = new double[NUM_MOVE_ACTIONS][NUM_MOVE_ACTIONS];

        for (int choose = 0; choose < NUM_MOVE_ACTIONS; choose++) {
            for (int outcome = 0; outcome < NUM_MOVE_ACTIONS; outcome++) {
                if (choose == outcome) {
                    moveDynamics[choose][outcome] = correctProb;
                }
                // the two directions which are one away get the rest of prob
                else if (Math.abs(choose - outcome) % 2 == 1) {
                    moveDynamics[choose][outcome] = (1 - correctProb) / 2;
                } else {
                    moveDynamics[choose][outcome] = 0;
                }
            }
        }
    }
    public void addStandardThrustValues() {
        this.thrustValues.add(0.02D);
    }
    public void addStandardThrustDirections() {
        this.directionValues.add(0.0);
        this.directionValues.add(0.5*Math.PI);
        this.directionValues.add(Math.PI);
        this.directionValues.add(1.5*Math.PI);
    }

    @Override
    public OOSADomain generateDomain(){

        setMoveDynamics(correctMoveprob);

        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_AGENT, LiftCopterAgent.class)
                .addStateClass(CLASS_CARGO, LiftCopterCargo.class)
                .addStateClass(CLASS_LOCATION, LiftCopterLocation.class)
                .addStateClass(CLASS_WALL,LiftCopterWall.class);

        addStandardThrustDirections();
        addStandardThrustValues();

        domain.addActionType(new PutdownActionType(ACTION_PUTDOWN, new String[]{CLASS_CARGO}))
                .addActionType(new PickupActionType(ACTION_PICKUP, new String[]{CLASS_CARGO}))
                .addActionType(new UniversalActionType(ACTION_IDLE))
                .addActionType(new ThrustType(this.thrustValues, this.directionValues));

        if (rf == null) {
            rf = new LiftCopterRewardFunction();
        }

        if (tf == null) {
            tf = new LiftCopterTerminalFunction();
        }
        LiftCopterModel model = new LiftCopterModel(moveDynamics);
        FactoredModel copterModel = new FactoredModel(model, rf, tf);
        domain.setModel(copterModel);
        return domain;
    }
    public static void main(String[] args) {

        LiftCopter copter = new LiftCopter();
        OOSADomain domain = copter.generateDomain();
        State s = LiftCopterStateFactory.createClassicState(2);

        Visualizer v = LiftCopterVisualizer.getVisualizer(5,5);



        VisualExplorer exp = new VisualExplorer(domain, v, s);

        exp.addKeyAction("w", "thrust", ACTION_THRUST_NORTH);
        exp.addKeyAction("d", "thrust", ACTION_THRUST_EAST);
        exp.addKeyAction("a", "thrust", ACTION_THRUST_SOUTH);
        exp.addKeyAction("s", "thrust", ACTION_THRUST_WEST);
        exp.addKeyAction("e", ACTION_PUTDOWN, "cargo0");
        exp.addKeyAction("q", ACTION_PICKUP, "cargo0");
        exp.addKeyAction("x", ACTION_IDLE,"");

        exp.initGUI();

    }

    @Override
    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }

    @Override
    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    @Override
    public RewardFunction getRf() {
        return rf;
    }

    @Override
    public TerminalFunction getTf() {
        return tf;
    }

    public List<Double> getThrustValues() {
        return thrustValues;
    }

    public void setThrustValues(List<Double> thrustValues) {
        this.thrustValues = thrustValues;
    }

    public List<Double> getDirectionValues() {
        return directionValues;
    }

    public void setDirectionValues(List<Double> directionValues) {
        this.directionValues = directionValues;
    }

    public double getCorrectMoveprob() {
        return correctMoveprob;
    }

    public void setCorrectMoveprob(double correctMoveprob) {
        this.correctMoveprob = correctMoveprob;
    }

    public double[][] getMoveDynamics() {
        return moveDynamics;
    }

    public void setMoveDynamics(double[][] moveDynamics) {
        this.moveDynamics = moveDynamics;
    }
}
