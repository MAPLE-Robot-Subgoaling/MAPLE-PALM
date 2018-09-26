package edu.umbc.cs.maple.jumper;

import burlap.behavior.functionapproximation.dense.DenseCrossProductFeatures;
import burlap.behavior.functionapproximation.dense.NormalizedVariableFeatures;
import burlap.behavior.functionapproximation.dense.fourier.FourierBasis;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.lspi.LSPI;
import burlap.behavior.singleagent.learning.lspi.SARSCollector;
import burlap.behavior.singleagent.learning.lspi.SARSData;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.domain.singleagent.mountaincar.MountainCar;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.DomainGoal;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailRF;
import edu.umbc.cs.maple.hierarchy.framework.GoalFailTF;
import edu.umbc.cs.maple.jumper.state.JumperAgent;
import edu.umbc.cs.maple.jumper.state.JumperState;
import edu.umbc.cs.maple.jumper.state.JumperStateGenerator;
import edu.umbc.cs.maple.jumper.state.JumperTarget;
import edu.umbc.cs.maple.utilities.ExceptionReward;
import edu.umbc.cs.maple.utilities.ExceptionTermination;
import edu.umbc.cs.maple.utilities.OOSADomainGenerator;

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

    public static void main(String[] args) {

        double minX = 0;
        double minY = 0;
        double maxX = 10.0;
        double maxY = 10.0;
        double jumpRadius = 1.0;
        double goalRadius = 2.0;

        DomainGoal goal = new JumperGoal(new AgentNearAnyTargetPF(goalRadius));
        GoalFailTF tf = new GoalFailTF(goal);
        GoalFailRF rf = new GoalFailRF(tf);
        JumperDomainGenerator jdg = new JumperDomainGenerator(minX, maxX, minY, maxY, jumpRadius, tf, rf);
        OOSADomain domain = jdg.generateDomain();
        JumperStateGenerator jsg = new JumperStateGenerator(minX, maxX, minY, maxY, jumpRadius);

        SARSCollector collector = new SARSCollector.UniformRandomSARSCollector(domain);
        int sampleCount = 10000;
        int maxEpisodeSteps = 1000;
        SARSData dataset = collector.collectNInstances(jsg, domain.getModel(), sampleCount, maxEpisodeSteps, null);

        NormalizedVariableFeatures inputFeatures = new NormalizedVariableFeatures()
                .variableDomain(ATT_X, new VariableDomain(jdg.minX, jdg.maxX))
                .variableDomain(ATT_Y, new VariableDomain(jdg.minY, jdg.maxY));

        int order = 4;
        FourierBasis fb = new FourierBasis(inputFeatures, order);

        double gamma = 0.99;
        int actionCount = 4;
        int iterationCount = 30;
        double maxChange = 1e-6;
        DenseCrossProductFeatures features = new DenseCrossProductFeatures(fb, actionCount);
        LSPI lspi = new LSPI(domain, gamma, features, dataset);
        Policy p = lspi.runPolicyIteration(iterationCount, maxChange);

        SimulatedEnvironment env = new SimulatedEnvironment(domain, jsg);

        int rolloutCount = 10;
        for (int i = 0; i < rolloutCount; i++){
            Episode e = PolicyUtils.rollout(p, env, maxEpisodeSteps);
            System.out.println(e.actionSequence);
            System.out.println(e.rewardSequence);
            env.resetEnvironment();
        }

        System.out.println("Finished");

    }

}
