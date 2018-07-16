package edu.umbc.cs.maple.testing;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import edu.umbc.cs.maple.palm.agent.AncestorPALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.CrossPALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.PALMLearningAgent;
import edu.umbc.cs.maple.palm.agent.PALMModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.ExpectedRmaxModelGenerator;
//import edu.umbc.cs.maple.palm.rmax.agent.ExpertNavModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.ExpertNavModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.PALMRmaxModelGenerator;
import edu.umbc.cs.maple.palm.rmax.agent.UpdatePALMRmaxModelGenerator;
import edu.umbc.cs.maple.rmaxq.agent.RmaxQLearningAgent;
import edu.umbc.cs.maple.state.hashing.bugfix.BugfixHashableStateFactory;
import edu.umbc.cs.maple.state.hashing.cached.CachedHashableStateFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_LEARNING_RATE;
import static edu.umbc.cs.maple.utilities.BurlapConstants.DEFAULT_Q_INIT;

public enum AgentType {

    PALM_EXPERT_CROSS("palmExpertCross", "PALM-Ex-Cross"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new PALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new CrossPALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    PALM_EXPERT_ANCESTOR("palmExpertAncestor", "PALM-Ex-Ancestor"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new UpdatePALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new AncestorPALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    PALM_EXPERT("palmExpert", "PALM-Expert"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new PALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    PALM_HIERGEN("palmHierGen", "PALM-HierGen"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new PALMRmaxModelGenerator(hsf, config);
            LearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }
    },
    PALM_EXPERT_NAV_GIVEN("palmExpertWithNavGiven", "PALM-Expert w/ Nav"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            PALMModelGenerator modelGen = new ExpertNavModelGenerator(hsf, config);
            LearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }

    },
    RMAXQ_EXPERT("rmaxqExpert", "RMAXQ-Expert"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            return new RmaxQLearningAgent(root, hsf, config);
        }

    },
    RMAXQ_HIERGEN("rmaxqHierGen", "RMAXQ-HierGen"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            return new RmaxQLearningAgent(root, hsf, config);
        }

    },

    KAPPA_EXPERT("kappaExpert", "κ-Expert"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            ExpectedRmaxModelGenerator modelGen = new ExpectedRmaxModelGenerator(hsf, config);
            PALMLearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }

    },
    KAPPA_HIERGEN("kappaHierGen", "κ-HierGen"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            ExpectedRmaxModelGenerator modelGen = new ExpectedRmaxModelGenerator(hsf, config);
            PALMLearningAgent agent = new PALMLearningAgent(root, modelGen, hsf, config);
            return agent;
        }

    },
    Q_LEARNING("qLearning", "QL"){
        @Override
        public LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config) {
            OOSADomain baseDomain = root.getDomain();
            double qInit = DEFAULT_Q_INIT;
            double learningRate = DEFAULT_LEARNING_RATE;
            LearningAgent agent = new QLearning(baseDomain, config.gamma, hsf, qInit, learningRate);
            return agent;
        }

    },

    ;

    private String type;
    private String plotterDisplayName;

    AgentType(String type, String plotterDisplayName) {
        this.type = type;
        this.plotterDisplayName = plotterDisplayName;
    }

    public String getType() {
        return type;
    }

    public String getPlotterDisplayName() {
        return plotterDisplayName;
    }

    public static List<String> getTypes() {
        return Arrays.stream(AgentType.values()).map(AgentType::getType).collect(Collectors.toList());
    }

    public abstract LearningAgent getLearningAgent(Task root, HashableStateFactory hsf, ExperimentConfig config);
    public LearningAgent getLearningAgent(Task root, ExperimentConfig config) {
        HashableStateFactory hsf = initializeHashableStateFactory(config.identifier_independent);
        return getLearningAgent(root, hsf, config);
    }


    public static HashableStateFactory initializeHashableStateFactory(boolean identifierIndependent) {
//        return new CachedHashableStateFactory(identifierIndependent);
//        return new SimpleHashableStateFactory(identifierIndependent);
        return new BugfixHashableStateFactory(identifierIndependent);
    }

    public static LearningAgentFactory generate(String agentTypeString, ExperimentConfig config, Task expertRoot, Task hierGenRoot, Task qLearningWrapper) {
        AgentType agentType = AgentType.getByType(agentTypeString);
        Task root = null;
        if (agentTypeString.contains("Expert")) {
            root = expertRoot;
        } else if (agentTypeString.contains("HierGen")) {
            root = hierGenRoot;
        } else if (agentTypeString.contains("qLearning")) {
            root = qLearningWrapper;
        } else {
            throw new RuntimeException("Unknown root task for " + agentType + " in AgentType");
        }
        return agentType.generateLearningAgentFactory(root, config);
    }

    public static AgentType getByType(String name) {
        for (AgentType type : values()) {
            if (type.type.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static final boolean DEFAULT_IDENTIFIER_INDEPENDENT = false;
    public LearningAgentFactory generateLearningAgentFactory(Task root, ExperimentConfig config) {
        LearningAgentFactory agent = new LearningAgentFactory() {

            @Override
            public String getAgentName() {
                return getPlotterDisplayName();
            }

            @Override
            public LearningAgent generateAgent() {
                return getLearningAgent(root, config);
            }
        };
        return agent;
    }
}
