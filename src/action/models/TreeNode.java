package action.models;

import burlap.mdp.core.state.State;
import utilities.MurmurHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNode {
	//a decision tree node

	/**
	 * the state variable that is tested at this node
	 */
	private String variable;

	/**
	 * a set of child nodes which are stored by the value of this node variable
	 */
	private Map<String, TreeNode> children;

	/**
	 * the class label
	 */
	private String classLabel;

	/**
	 * create a ned internal decision tree
	 * @param variable the variable to test at this node
	 * @param vals the child nodes
	 */
	public TreeNode(String variable, Map<String, TreeNode> vals) {
		this.variable = variable;
		this.children = vals;
		this.classLabel = null;
	}

	/**
	 * create a leaf node
	 * @param val the class label
	 */
	public TreeNode(String val) {
		this.classLabel = val;
	}


	/**
	 * return the label for the given state
	 * @param s the state to classify
	 * @return the state's label
	 */
	public String classify(State s) {
		if (classLabel != null)
			return classLabel;
		Object val = s.get(variable);
		String value;

		//weka putsas ints and doubles into the tree differently
		// so int and double types must be check to match the child states
		if (val instanceof Number) {
			if(((Number) val).doubleValue() == ((Number) val).intValue())
				value = ((Number) val).intValue() + "";
			else
				value = ((Number) val).doubleValue() + "";
		} else {
			value = MurmurHash.hash32(val.toString()) + "";
		}
		TreeNode child = children.get(value);
		return child.classify(s);
	}

	/**
	 * retur a list of state variables which were at the
	 * node's path to the base of the tree
	 * @param s the state of interest
	 * @return the llst of check variables for s
	 */
	public List<String> getCheckedVariables(State s){
		List<String> checks = new ArrayList<String>();
		getCheckedVariables(s, checks);
		return checks;
	}

	/**
	 * recursively gather the checked variables for a given state
	 * @param s the state of interest
	 * @param vars
	 */
	public void getCheckedVariables(State s, List<String> vars){
		if (classLabel != null)
			return;

		//this is the same check as classify except at each step
		//the variable is recorded as checked
		vars.add(variable);
		Object val = s.get(variable);
		String value;
		if (val instanceof Number) {
			if(((Number) val).doubleValue() == ((Number) val).intValue())
				value = ((Number) val).intValue() + "";
			else
				value = ((Number) val).doubleValue() + "";
		} else {
			value = MurmurHash.hash32(val.toString()) + "";
		}
		TreeNode child = children.get(value);
		child.getCheckedVariables(s, vars);
	}

	/**
	 * get the class label
	 * @return the class label of a leaf node or null for an internal node
	 */
	public String getLabel() {
		return classLabel;
	}

	@Override
	public String toString() {
		String out = "";
		if (classLabel != null)
			return ": " + classLabel;
		else {
			for (String val : children.keySet()) {
				out += "\n" + variable + " = " + val;
				TreeNode child = children.get(val);
				if (child.getLabel() != null)
					out += child.toString();
				else {
					String childStr = child.toString();
					out += childStr.replaceAll("\n", "\n|    ");
				}
			}
			return out;
		}
	}
}
