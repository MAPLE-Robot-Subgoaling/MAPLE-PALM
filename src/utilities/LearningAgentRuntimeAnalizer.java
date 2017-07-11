package utilities;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.singleagent.environment.Environment;

public class LearningAgentRuntimeAnalizer {

	private int chartWidth;
	private int chartHieght;
	
	private List<LearningAgentFactory> agentFactoties;
	private Map<String, List<XYSeries>> trialData;
	private ChartPanel trialPanel, averagePanel;
	private YIntervalSeriesCollection averages;
	private List<XYSeriesCollection> trials;
	private JFreeChart trialChart;
	
	private double significanceLevel;
	private static final Map<Integer, Double> cachedCriticalValues = new HashMap<Integer, Double>();
	
	public LearningAgentRuntimeAnalizer(int chartWidth, int chartHieght, LearningAgentFactory... agentFactories) {
		this.chartHieght = chartHieght;
		this.chartWidth = chartWidth;
		this.agentFactoties = new ArrayList<LearningAgentFactory>();
		this.trialData = new HashMap<String, List<XYSeries>>();
		this.trials = new ArrayList<XYSeriesCollection>();
		this.significanceLevel = 0.05;
		for(LearningAgentFactory agent : agentFactories){
			this.agentFactoties.add(agent);
			this.trialData.put(agent.getAgentName(), new ArrayList<XYSeries>());
		}
	}
	
	public void setSignificance(double sig){
		this.significanceLevel = sig;
	}
	
	public void reset(){
		
	}
	
	public void runRuntimeAnalysis(int trials, int episodesPerTrial, int maxStepsPerEpisode, Environment env){
		showCharts(trials);
		for(int trial = 0; trial < trials; trial++)
			newTrial();
			
		for(LearningAgentFactory agentMaker : agentFactoties){
			for(int trial = 0; trial < trials; trial++){
				XYSeriesCollection trialSet = this.trials.get(trial);
				trialChart.getXYPlot().setDataset(trialSet);
				
				XYSeries agentTimes = this.trialData.get(agentMaker.getAgentName()).get(trial);
				LearningAgent agent = agentMaker.generateAgent();
				for(int episode = 0; episode < episodesPerTrial; episode++){
					long time = System.currentTimeMillis();
					agent.runLearningEpisode(env, maxStepsPerEpisode);
					time = System.currentTimeMillis() - time;
					double secs = time / 1000.0;
					agentTimes.add(episode + 1, secs);
				}
				env.resetEnvironment();
			}
			if(trials > 1){
				addAverage(agentMaker.getAgentName());
			}
		}
	}
	
	private void addAverage(String agentName){
		List<XYSeries> trials = trialData.get(agentName);
		YIntervalSeries avgs = new YIntervalSeries(agentName);
		for(int i = 0; i < trials.get(0).getItemCount(); i++){
			DescriptiveStatistics avg = new DescriptiveStatistics();
			for(XYSeries trial : trials){
				avg.addValue(trial.getDataItem(i).getYValue());
			}
			double[] ci = getCI(avg, significanceLevel);
			avgs.add(i, ci[0], ci[1], ci[2]);
		}
		averages.addSeries(avgs);
	}
	
	private void showCharts(int trials){
		JFrame frame = new JFrame();
		
		Container pane = new Container();
		pane.setLayout(new FlowLayout());
			
		JFreeChart trialChart = ChartFactory.createXYLineChart
				("Leaning Agent Runtime", "Episode", "Seconds", null);
		trialPanel = new ChartPanel(trialChart);
		trialPanel.setPreferredSize(new Dimension(chartWidth, chartHieght));
		pane.add(trialPanel);
	
		if(trials > 1){
			averages = new YIntervalSeriesCollection();
			JFreeChart avgChart = ChartFactory.createXYLineChart("Avergage Runtime", "Episodes", "Seconds", averages);
			averagePanel = new ChartPanel(avgChart);
			averagePanel.setPreferredSize(new Dimension(chartWidth, chartHieght));
			pane.add(averagePanel);
		}
		
		frame.add(pane);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void newTrial(){
		XYSeriesCollection trial1 = new XYSeriesCollection();
		for(LearningAgentFactory agent : agentFactoties){
			XYSeries trial = new XYSeries(agent.getAgentName());
			trial1.addSeries(trial);
			this.trialData.get(agent.getAgentName()).add(trial);
		}
		
		trialChart = ChartFactory.createXYLineChart
				("Leaning Agent Runtime", "Episode", "Seconds", trial1);
		trialPanel.setChart(trialChart);
		trials.add(trial1);
	}
	
	public static double [] getCI(DescriptiveStatistics stats, double significanceLevel){
		
		int n = (int)stats.getN();
		Double critD = cachedCriticalValues.get(n-1);
		if(critD == null){
			TDistribution tdist = new TDistribution(stats.getN());
			double crit = tdist.inverseCumulativeProbability(1. - (significanceLevel/2.));
			critD = crit;
			cachedCriticalValues.put(n-1, critD);
		}
		double crit = critD;
		double width = crit * stats.getStandardDeviation() / Math.sqrt(stats.getN());
		double m = stats.getMean();
		return new double[]{m, m-width, m+width};
	}
}
