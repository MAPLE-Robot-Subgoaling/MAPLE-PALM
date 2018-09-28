package edu.umbc.cs.maple.jumper;

import burlap.behavior.functionapproximation.dense.DenseCrossProductFeatures;
import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.functionapproximation.dense.rbf.DistanceMetric;
import burlap.behavior.functionapproximation.dense.rbf.RBFFeatures;
import burlap.behavior.functionapproximation.dense.rbf.functions.GaussianRBF;
import burlap.behavior.functionapproximation.dense.rbf.metrics.EuclideanDistance;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.gridset.FlatStateGridder;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.lspi.SARSCollector;
import burlap.behavior.singleagent.learning.lspi.SARSData;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.domain.singleagent.mountaincar.MCRandomStateGenerator;
import burlap.domain.singleagent.mountaincar.MCState;
import burlap.domain.singleagent.mountaincar.MountainCar;
import burlap.domain.singleagent.mountaincar.MountainCarVisualizer;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperState;
import edu.umbc.cs.maple.jumper.state.JumperStateGenerator;
import edu.umbc.cs.maple.jumper.state.JumperTarget;
import edu.umbc.cs.maple.utilities.BurlapConstants;
import edu.umbc.cs.maple.utilities.ExceptionReward;
import edu.umbc.cs.maple.utilities.ExceptionTermination;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static edu.umbc.cs.maple.jumper.JumperConstants.*;

public class JumperDomainGenerator extends OOSADomainGenerator {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double jumpRadius;

    private TerminalFunction tf;
    private RewardFunction rf;

    public JumperDomainGenerator(double minX, double maxX, double minY, double maxY, double jumpRadius, TerminalFunction tf, RewardFunction rf) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.jumpRadius = jumpRadius;
        this.tf = tf;
        this.rf = rf;

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
    public TerminalFunction getTf() {
        return tf;
    }

    @Override
    public RewardFunction getRf() {
        return rf;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    @Override
    public OOSADomain generateDomain() {
        JumperModel model = new JumperModel(minX, maxX, minY, maxY, jumpRadius);
        if (rf == null) { rf = new ExceptionReward(); }
        if (tf == null) { tf = new ExceptionTermination(); }
        FactoredModel fModel = new FactoredModel(model, rf, tf);
        OOSADomain domain = new OOSADomain();
        domain.setModel(fModel);
        domain.addStateClass(CLASS_AGENT, JumperAgent.class);
        domain.addStateClass(CLASS_TARGET, JumperTarget.class);
        domain.addActionType(new UniversalActionType(ACTION_NORTH));
        domain.addActionType(new UniversalActionType(ACTION_SOUTH));
        domain.addActionType(new UniversalActionType(ACTION_EAST));
        domain.addActionType(new UniversalActionType(ACTION_WEST));
        return domain;
    }

    public static void LSPI(JumperDomainGenerator jdg, StateGenerator jsg, SARSData dataset, SADomain domain, int maxEpisodeSteps) {

        // MAJOR problem on OOVariableKey, how it handles equals() method is using == so if the key is not literally
        // the same object, the features in NormalizedVariableFeatures.features() will silently "continue" and do nothing
        NormalizedVariableFeatures inputFeatures = new NormalizedVariableFeatures()
                .variableDomain("agent0:x", new VariableDomain(jdg.minX, jdg.maxX))
                .variableDomain("agent0:y", new VariableDomain(jdg.minY, jdg.maxY))
                .variableDomain("target0:x", new VariableDomain(jdg.minX, jdg.maxX))
                .variableDomain("target0:y", new VariableDomain(jdg.minY, jdg.maxY));

//        int order = 2; //4;
//        FourierBasis inFeatures = new FourierBasis(inputFeatures, order);
        int gridPointCount = 2; //5;
        RBFFeatures inFeatures = new RBFFeatures(inputFeatures, true);
        FlatStateGridder gridder = new FlatStateGridder()
                .gridDimension("agent0:x", jdg.minX, jdg.maxX, gridPointCount)
                .gridDimension("agent0:y", jdg.minX, jdg.maxX, gridPointCount)
                .gridDimension("target0:x", jdg.minX, jdg.maxX, gridPointCount)
                .gridDimension("target0:y", jdg.minX, jdg.maxX, gridPointCount)
                ;
        double epsilon = 2.0;
        MutableState initialState = (MutableState) jsg.generateState();
        List<State> griddedStates = gridder.gridState(initialState);
        DistanceMetric metric = new EuclideanDistance();
        for (State g : griddedStates) {
            inFeatures.addRBF(new GaussianRBF(inputFeatures.features(g), metric, epsilon));
        }

        double gamma = 0.99;
        int actionCount = 4;
        int iterationCount = 100;
        double maxChange = 1e-6;
        DenseCrossProductFeatures features = new DenseCrossProductFeatures(inFeatures, actionCount);
        LSPI lspi = new LSPI(domain, gamma, features, dataset);
        Policy p = lspi.runPolicyIteration(iterationCount, maxChange);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, jsg);

        int rolloutCount = 10;
        for (int i = 0; i < rolloutCount; i++){
            Episode e = PolicyUtils.rollout(p, env, maxEpisodeSteps);
            System.out.println("\n");
            System.out.println(e.stateSequence.get(0));
            System.out.println(e.stateSequence.get(e.stateSequence.size()-1));
            System.out.println(e.actionSequence);
            System.out.println(e.rewardSequence);
            env.resetEnvironment();
        }

        System.out.println("Finished");


    }


    public static void main(String[] args) {

        RandomFactory.getMapped(BurlapConstants.DEFAULT_RNG_INDEX).setSeed(24L);

        double minX = 0;
        double minY = 0;
        double maxX = 1.0;
        double maxY = 1.0;
        double jumpRadius = 0.1;
        double goalRadius = 0.2;

        DomainGoal goal = new JumperGoal(new AgentNearAnyTargetPF(goalRadius));
        GoalFailTF tf = new GoalFailTF(goal);
        GoalFailRF rf = new GoalFailRF(tf);
        JumperDomainGenerator jdg = new JumperDomainGenerator(minX, maxX, minY, maxY, jumpRadius, tf, rf);
        OOSADomain domain = jdg.generateDomain();
        StateGenerator jsg = new JumperStateGenerator(minX, maxX, minY, maxY, goalRadius);

        System.err.println("Using constant state");
        System.err.println("Using constant state");
        System.err.println("Using constant state");
        jsg = new ConstantStateGenerator(jsg.generateState());

//        SimulatedEnvironment env = new SimulatedEnvironment(domain, jsg);
//        float width = (float) (maxX - minX);
//        float height = (float) (maxY - minY);
//        Visualizer v = JumperVisualizer.getVisualizer(width, height);
//        VisualExplorer exp = new VisualExplorer(domain, env, v);
//        exp.addKeyAction("w", ACTION_NORTH, "");
//        exp.addKeyAction("s", ACTION_SOUTH, "");
//        exp.addKeyAction("d", ACTION_EAST, "");
//        exp.addKeyAction("a", ACTION_WEST, "");
//        exp.initGUI();
//        exp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SARSCollector collector = new SARSCollector.UniformRandomSARSCollector(domain);
        int sampleCount = 10000;
        int maxEpisodeSteps = 100;
        SARSData dataset = collector.collectNInstances(jsg, domain.getModel(), sampleCount, maxEpisodeSteps, null);

        List<SARSData.SARS> toRemove = new ArrayList<>();
        int count = 0;
        double totalReward = 0.0;
        int scale = 10;
        for (SARSData.SARS d : dataset.dataset) {
            if(d.r > 0) {
                totalReward += d.r;
                count += 1;
            } else {
                toRemove.add(d);
            }
        }
        System.out.println(totalReward + " " + count);
        for (SARSData.SARS d : toRemove) {
            dataset.dataset.remove(d);
            if (dataset.dataset.size() <= count*scale) {
                break;
            }
        }
        System.out.println(dataset.dataset.size());

        LSPI(jdg,jsg,dataset,domain,maxEpisodeSteps);

    }



}
