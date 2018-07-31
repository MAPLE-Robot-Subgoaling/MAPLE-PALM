package edu.umbc.cs.maple.testing;


public class RuntimeTest {
//    public static void createCharts(final State s, OOSADomain domain, final Task palmRoot, final Task rmaxqRoot,
//                                    final double rmax, final int threshold, final double maxDelta, final double discount,
//                                    int numEpisode, int maxSteps, int numTrial, int maxIterationsInModel,
//                                    boolean useMultitimeModel, boolean waitForChildren){
//        final HashableStateFactory hs = new SimpleHashableStateFactory(true);
//        final GroundedTask groundedPalmRoot = palmRoot.getAllGroundedTasks(s).get(0);
//
//        SimulatedEnvironment env = new SimulatedEnvironment(domain, s);
////		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
////        obs.initGUI();
////        obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
////        env.addObservers(obs);
//
//        LearningAgentFactory rmaxq = new LearningAgentFactory() {
//
//            @Override
//            public String getAgentName() {
//                return AgentType.RMAXQ_EXPERT.getPlotterDisplayName();
//            }
//
//            @Override
//            public LearningAgent generateAgent() {
//                throw new RuntimeException("not fixed on this branch");
////				return new RmaxQLearningAgent(rmaxqRoot, hs, s, rmax, threshold, maxDelta);
//            }
//        };
//
//        LearningAgentFactory palm = new LearningAgentFactory() {
//
//            @Override
//            public String getAgentName() {
//                return AgentType.PALM_EXPERT.getPlotterDisplayName();
//            }
//
//            @Override
//            public LearningAgent generateAgent() {
//                PALMRmaxModelGenerator modelGen = new PALMRmaxModelGenerator(threshold,
//                        rmax, hs, discount, useMultitimeModel);
//                return new PALMLearningAgent(groundedPalmRoot,modelGen, hs, maxDelta,
//                        maxIterationsInModel, waitForChildren);
//            }
//        };
//
//        LearningAgentRuntimeAnalizer timer = new LearningAgentRuntimeAnalizer(500, 300, palm, rmaxq);
//        timer.runRuntimeAnalysis(numTrial, numEpisode, maxSteps, env);
//        timer.writeDataToCSV("./results/");
//    }


    public static void main(String[] args) {
//        double correctMoveprob = 1;
//        double fickleProb = 0;
//        int numEpisodes = 30;
//        int maxSteps = 2000;
//        int rmaxThreshold = 1;
//        int numTrials = 5;
//        double gamma = 0.9;
//        double rmax = 20;
//        double maxDelta = 0.01;
//        int maxIterationsInModel = 10000;
//        boolean useMultitimeModel = true;
//        boolean waitForChildren = true;
//
//        TaxiState s = TaxiStateFactory.createTinyState();
//        TaxiHierarchy amdpHierarchy = new TaxiHierarchyExpert();
//        TaxiHierarchy rmaxqHierarchy = new TaxiHierarchyExpert();
//        Task palmRoot = amdpHierarchy.createHierarchy(correctMoveprob, fickleProb,  false);
//        Task rmaxqRoot = rmaxqHierarchy.createHierarchy(correctMoveprob, fickleProb, false);
//        OOSADomain base = amdpHierarchy.getBaseDomain();
//        amdpHierarchy.setBaseDomain(base);
//        rmaxqHierarchy.setBaseDomain(base);
//        createCharts(s, base, palmRoot, rmaxqRoot, rmax, rmaxThreshold, maxDelta, gamma,
//                numEpisodes, maxSteps, numTrials, maxIterationsInModel, useMultitimeModel, waitForChildren);
        throw new RuntimeException("TODO: update");
    }
}