package action.models;

import java.util.Map;

public class TreeNode {

    private String variable;
    private Map<String, TreeNode> children;
    private String value;

    public TreeNode(String variable, Map<String, TreeNode> vals){
        this.variable = variable;
        this.children = vals;
        value = null;
    }

    public TreeNode(String val){
        this.value = val;
    }

    public TreeNode getChild(String value){
        return children.get(value);
    }

    public String getTestVariable() {
        return variable;
    }

    public String getValue() {
        return value;
    }

    public int getNumChildren(){
        return children.size();
    }

    @Override
    public String toString() {
        String out = "";
        if(value != null)
            return  ": " +value;
        else {
            for (String val : children.keySet()) {
                out += "\n" + variable + " = " + val;
                TreeNode child = children.get(val);
                if(child.getValue() != null)
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
