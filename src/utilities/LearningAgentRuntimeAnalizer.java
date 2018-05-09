package utilities;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.mdp.singleagent.environment.Environment;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearningAgentRuntimeAnalizer {

	private int chartWidth;
	private int chartHieght;
	
	private int delay = 1000;
	private int timestep = 0;
	private int lastUpdated = 0;
	
	private int currentTrial;
	private List<LearningAgentFactory> agentFactoties;
	private Map<String, List<XYSeries>> trialData;
	private ChartPanel trialPanel, averagePanel;
	private YIntervalSeriesCollection averages;
	private List<XYSeriesCollection> trials;
	private JFreeChart trialChart;
	private String currentAgent = "";
	
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
			for(currentTrial = 0; currentTrial < trials; currentTrial++){
				System.out.println("Beginning " + agentMaker.getAgentName() + " trial " + (currentTrial + 1)
						+ "/" + trials);
				
				XYSeriesCollection trialSet = this.trials.get(currentTrial);
				trialChart.getXYPlot().setDataset(trialSet);
				
				XYSeries agentTimes = this.trialData.get(agentMaker.getAgentName()).get(currentTrial);
				LearningAgent agent = agentMaker.generateAgent();
				currentAgent = agentMaker.getAgentName();
				timestep = 0;
				for(int episode = 0; episode < episodesPerTrial; episode++){
					long time = System.currentTimeMillis();
					agent.runLearningEpisode(env, maxStepsPerEpisode);

					time = System.currentTimeMillis() - time;
					double secs = time / 1000.0;
					agentTimes.add(episode + 1, secs);
					timestep++;
					env.resetEnvironment();
				}
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
			((XYPlot)avgChart.getPlot()).setRenderer(this.createDeviationRenderer());
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
	
	public void writeDataToCSV(String filePath){
		if(!filePath.endsWith(".csv")){
			filePath = filePath + ".csv";
		}
		
		try {
			BufferedWriter outFile = new BufferedWriter(new FileWriter(filePath));
			
			for(String agentName : trialData.keySet()){
				outFile.write("\n,," + agentName + "'s Runtimes by trial\n");
				outFile.write("Episode,");
				
				List<XYSeries> trials = trialData.get(agentName);
				for(int i = 1; i <= trials.size(); i++)
					outFile.write(i + ",");
				outFile.write("\n");
				
				for(int episode = 0; episode < trials.get(0).getItemCount(); episode++){
					outFile.write((episode + 1) + ",");
					for(int trial = 0; trial < trials.size(); trial++){
						XYSeries data = trials.get(trial);
						double time = data.getDataItem(episode).getYValue();
						outFile.write(time + ","); 
					}
					
//					outFile.write("AVERGA);
					outFile.write("\n");
				}
				outFile.write("\n\n");
			}
			outFile.close();
			
		} catch (Exception e) {
			System.err.println("Could not write csv file to: " + filePath);
			e.printStackTrace();
		}
	}
	
	protected void launchThread(){
		 Thread refreshThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true){
						LearningAgentRuntimeAnalizer.this.updateTimeSeries();
						try {
							Thread.sleep(LearningAgentRuntimeAnalizer.this.delay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			});
	        
	       refreshThread.start();
		 	
	}
	
	synchronized protected void updateTimeSeries(){
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				synchronized (LearningAgentRuntimeAnalizer.this) {
					if(LearningAgentRuntimeAnalizer.this.timestep > LearningAgentRuntimeAnalizer.this.lastUpdated){
						LearningAgentRuntimeAnalizer.this.lastUpdated = LearningAgentRuntimeAnalizer.this.timestep;
						List<XYSeries> trials = LearningAgentRuntimeAnalizer.this.trialData.get
								(LearningAgentRuntimeAnalizer.this.currentAgent);
						XYSeries trial = trials.get(LearningAgentRuntimeAnalizer.this.currentTrial);
						trial.fireSeriesChanged();
					}
				}
			}
		});
	}
	
	protected DeviationRenderer createDeviationRenderer(){
		DeviationRenderer renderer = new DeviationRenderer(true, false);
		
		for(int i = 0; i < DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length; i++){
			Color c = (Color)DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
			Color nc = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
			renderer.setSeriesFillPaint(i, nc);
		}
		
		return renderer;
	}
}
