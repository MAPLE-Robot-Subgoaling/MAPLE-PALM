package action.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddValues;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class CreateActionModels {

	
	public static void createModels(List<Episode> trajectories){
		List<String> actions = new ArrayList<String>();
		for(Episode e : trajectories){
			for(Action a : e.actionSequence){
				if(!actions.contains(a.toString()))
					actions.add(a.toString());
			}
		}
		
		for(String action : actions){
			List<State> priorStates = new ArrayList<State>();
			List<State> postStates = new ArrayList<State>();
			List<Double> rewards = new ArrayList<Double>();
			for(Episode trajectory : trajectories){
				for(int i = 0; i < trajectory.actionSequence.size(); i++){
					if(trajectory.actionSequence.get(i).toString().equals(action)){
						priorStates.add(trajectory.stateSequence.get(i));
						postStates.add(trajectory.stateSequence.get(i + 1));
						rewards.add(trajectory.rewardSequence.get(i));
					}
				}
			}
			
			List<Object>  variables = trajectories.get(0).stateSequence.get(0).variableKeys();
			for(Object var : variables){
				//create tree for each state variable with class as the next state's variable
				ArrayList<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute(var.toString() + "_prime"));
				
				for(Object variable : variables){
					
					attributes.add(new Attribute(variable.toString()));
				}
				Instances dataset = new Instances(action + "_" + var.toString(), attributes, priorStates.size());
				dataset.setClassIndex(0);
				
				//fill with data
				for(int i = 0; i < priorStates.size(); i++){
					Instance dataPoint = new DenseInstance(variables.size() + 1);
					dataPoint.setDataset(dataset);
					
					Object label = postStates.get(i).get(var);
					if(label instanceof Number){
						dataPoint.setValue(0, ((Number) label).doubleValue());
					}else{
						dataPoint.setValue(0, label.hashCode());
					}
					
					State prior = priorStates.get(i);
					addSStateVars(variables, dataPoint, prior);
					dataset.add(dataPoint);
				}
				
				J48 tree = buildTree(dataset);
				writeTreeToFile(action, var.toString(), tree);
			}
			
			//create tree for reward with claas as reward
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			attributes.add(new Attribute("reward"));
			for(Object var : variables){
				attributes.add(new Attribute(var.toString()));
			}
			Instances dataset = new Instances(action + "_Reward", attributes, priorStates.size());
			dataset.setClassIndex(0);
			
			for(int i = 0; i < priorStates.size(); i++){
				Instance dataPoint = new DenseInstance(variables.size() + 1);
				dataPoint.setDataset(dataset);
				
				double label = rewards.get(i);
				dataPoint.setValue(0, label);
				
				State prior = priorStates.get(i);
				addSStateVars(variables, dataPoint, prior);
				dataset.add(dataPoint);
			}
			J48 tree = buildTree(dataset);
			writeTreeToFile(action, "R", tree);
		}
	}

	private static void addSStateVars(List<Object> variables, Instance dataPoint, State prior) {
		int counter = 1;
		for(Object varKey : variables){
			Object value = prior.get(varKey);
			if(value instanceof Number){
				dataPoint.setValue(counter++, ((Number) value).doubleValue());
			}else{
				dataPoint.setValue(counter++, value.hashCode());
			}
		}
	}

	private static J48 buildTree(Instances dataset) {
		//apply filters
		try{
			NumericToNominal filterStrings = new NumericToNominal();
			String[] args = new String[2];
			args[0] = "-R";
			args[1] = "first";
			filterStrings.setOptions(args);
			filterStrings.setInputFormat(dataset);
			dataset = Filter.useFilter(dataset, filterStrings);
			
			AddValues addval = new AddValues();
			String[] argVals = {"-C", "first", "-L", "_"};
			addval.setOptions(argVals);
			addval.setInputFormat(dataset);
			dataset = Filter.useFilter(dataset, addval);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//train tree
		J48 tree = new J48();
		try{
			tree.buildClassifier(dataset);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return tree;
	}
	
	public static void writeTreeToFile(String action, String variable, J48 tree){
		String file = "trees/" + action + "_" + variable.replace(":", "_") + ".model";
		
		System.out.println(file);
		try {
			File folder = new File("trees");
			if(!folder.isDirectory())
				folder.mkdir();
			File f = new File(file);
			f.createNewFile();
			SerializationHelper.write(file, tree);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
