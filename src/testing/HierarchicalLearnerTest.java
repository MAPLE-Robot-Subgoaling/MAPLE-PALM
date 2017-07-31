package testing;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import hierarchy.framework.GroundedTask;
import hierarchy.framework.Task;
import ramdp.agent.RAMDPLearningAgent;
import rmaxq.agent.RmaxQLearningAgent;
import taxi.TaxiVisualizer;
import taxi.hierarchies.TaxiHierarchy;
import taxi.state.TaxiState;

import taxi.stateGenerator.RandomPassengerClassicTaxiState;
import taxi.stateGenerator.RandomPassengerSmallTaxiState;
import taxi.stateGenerator.TaxiStateFactory;
import taxi.stateGenerator.UniformPassengerClassicTaxiState;

public class HierarchicalLearnerTest {


    public static void runRAMDPEpisodes(int numEpisode, int maxSteps, Task root,
			State initial, OOSADomain groundDomain,
			int threshold, double discount, double rmax, double maxDelta,
            boolean relearn, int relearnThreshold, int lowerThreshold){
		List<Episode> episodes = new ArrayList<Episode>();
		GroundedTask rootgt = root.getAllGroundedTasks(initial).get(0);

		RAMDPLearningAgent ramdp =
                new RAMDPLearningAgent(rootgt, threshold, discount, rmax,
				new SimpleHashableStateFactory(true), maxDelta,
                relearn, relearnThreshold, lowerThreshold);
        UniformPassengerClassicTaxiState uniSG = new UniformPassengerClassicTaxiState();
        System.out.println("next ep--Pass goal: "+getColor(uniSG.getPassengerStartGoal()));
        System.out.println("---------Pass start: "+getColor(uniSG.getPassengerStartLocation()));
        SimulatedEnvironment env = new SimulatedEnvironment(groundDomain, uniSG);
//		VisualActionObserver obs = new VisualActionObserver(groundDomain, TaxiVisualizer.getVisualizer(5, 5));
//		obs.initGUI();
//		obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
//		env.addObservers(obs);
		for(int i = 0; i < numEpisode; i++){

            long time = System.currentTimeMillis();
            ramdp.goal = getColor(uniSG.getPassengerStartGoal());
            ramdp.start = getColor(uniSG.getPassengerStartLocation());
			Episode e = ramdp.runLearningEpisode(env, maxSteps);
			time = System.currentTimeMillis() - time;
			episodes.add(e);
			System.out.println("##### Episode " + i + " time " + (double)time/1000 );
            System.out.println("next ep--Pass goal: "+getColor(uniSG.getPassengerStartGoal()));
            System.out.println("---------Pass start: "+getColor(uniSG.getPassengerStartLocation()));
			env.resetEnvironment();
		}
		ramdp.first32();
		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(5, 5), groundDomain, episodes);
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}
    private static String getColor(int x){
        switch(x){
            case 0: return "red";
            case 1: return "yellow";
            case 2: return "blue";
            case 3: return "green";
            default: return "$$$$";
        }
    }
	public static void runRMAXQEpsodes(int numEpisodes, int maxSteps, Task root, State initState, double vmax,
			int threshold, double maxDelta, OOSADomain domain){
		HashableStateFactory hs = new SimpleHashableStateFactory(true);
		
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initState);
//		VisualActionObserver obs = new VisualActionObserver(domain, TaxiVisualizer.getVisualizer(5, 5));
//		obs.initGUI();
//		obs.setDefaultCloseOperation(obs.EXIT_ON_CLOSE);
//		env.addObservers(obs);
		
		List<Episode> episodes = new ArrayList<Episode>();
		RmaxQLearningAgent rmaxq = new RmaxQLearningAgent(root, hs, initState, vmax, threshold, maxDelta);
		
		for(int i = 1; i <= numEpisodes; i++){
			Episode e = rmaxq.runLearningEpisode(env, maxSteps);
			episodes.add(e);
			System.out.println("Episode " + i + " time " + rmaxq.getTime() / 1000.0);
			env.resetEnvironment();
		}
		
		EpisodeSequenceVisualizer ev = new EpisodeSequenceVisualizer
				(TaxiVisualizer.getVisualizer(5, 5), domain, episodes);
		ev.setDefaultCloseOperation(ev.EXIT_ON_CLOSE);
		ev.initGUI();
	}


	public static void main(String[] args) {

        int numTrials = 5;

        double correctMoveprob = 1;
		double fickleProb = .03;
//        double fickleProb = .25;
		int maxSteps = 1000;
		int rmaxThreshold = 3;
		double gamma = 0.9;
		double rmax = 20;
		double maxDelta = 0.01;

//        int numEpisodes = 250;
//        int actionRelearn = 0;
        int numEpisodes = 600;

        int episodeRelearn = 300;
        int actionRelearn = 20000;
        int relearnThreshold = 3;
		TaxiState s = TaxiStateFactory.createClassicState();
		Task RAMDProot = TaxiHierarchy.createAMDPHierarchy(correctMoveprob, fickleProb, false);

		OOSADomain base = TaxiHierarchy.getBaseDomain();
//		Task RMAXQroot = TaxiHierarchy.createRMAXQHierarchy(correctMoveprob, fickleProb);
		for(int i = 1; i <= numTrials; i++)
		    runRAMDPEpisodes(numEpisodes, maxSteps, RAMDProot, s, base, rmaxThreshold, gamma, rmax, maxDelta,
                    true, episodeRelearn, 0);
//		runRMAXQEpsodes(numEpisodes, maxSteps, RMAXQroot, s, rmax, rmaxThreshold, maxDelta, base);
	}
}
