package action.models;

import burlap.mdp.core.state.State;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableTree {

	/**
	 * the root node of the tree
	 */
	private TreeNode root;

	/**
	 * the string version oof the tree
	 */
    private String treeStr;

	/**
	 * parses the string to create the nodes of the tree
 	 * @param tree the test of the tree
	 */
	public VariableTree(String tree){
        treeStr = tree;
        root = read();
    }

	/**
	 * parses the file to create the tree's nodes
	 * @param tree the file containing the tree
	 */
	public VariableTree(File tree){
        root = read(tree);
    }

	/**
	 * reads in the tree from the tree
	 * @param file the file with the tree
	 * @return the tree
	 */
	protected TreeNode read(File file){
        try {
            String tree = new String(Files.readAllBytes(file.toPath()));
            treeStr = tree;
            return read();
//            System.out.println(tree);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

	/**
	 * recursively read iin the tree to build out the tree
	 * @return the current root of the tree
	 */
	protected TreeNode read(){

        int depth = firsltAlphaNumChar(treeStr);
        int lineDepth = depth;

		//take care of a leaf node
        String line = treeStr.substring(0, treeStr.indexOf("\n"));
        if(line.indexOf("=") == -1) {
            String value = line.substring(line.indexOf(": ") + 2);
            return new TreeNode(value);
        }

        String test = line.substring(lineDepth, line.indexOf(" ", lineDepth));
        Map<String, TreeNode> children = new HashMap<String, TreeNode>();

        //create a child  node for each value
        while (lineDepth == depth){
            line = treeStr.substring(0, treeStr.indexOf("\n"));
            treeStr = treeStr.substring(treeStr.indexOf("\n") + 1);

            //childen with only leaf children
            if(line.indexOf(": ") != -1){
                String value = line.substring(line.indexOf(": ") + 2);
                String testVal = line.substring(line.indexOf("=") + 2, line.indexOf(": "));
                TreeNode leaf = new TreeNode(value);

                children.put(testVal, leaf);
            }else { //test node
                String testVal = line.substring(line.indexOf("=") + 2);
                TreeNode child = read();
                children.put(testVal, child);
            }
            lineDepth = firsltAlphaNumChar(treeStr);
        }

        return new TreeNode(test, children);
    }

	/**
	 * find the index of the first letter or number in the string
	 * @param test the string to test
	 * @return the index of the first letter or number
	 */
	protected int firsltAlphaNumChar(String test){
        Pattern p = Pattern.compile("[A-Za-z0-9]");
        Matcher m = p.matcher(test);
        if(m.find())
            return m.start();
        else
            return -1;
    }

	/**
	 * begin to classify at the root
	 * @param s the state to test
	 * @return the class label
	 */
	public String classify(State s){
    	return root.classify(s);
	}

	/**
	 * begin to collect the checked variables
	 * @param s the state of interest
	 * @return the list of checked variables
	 */
	public List<String> getCheckedVariables(State s){
        return root.getCheckedVariables(s);
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
