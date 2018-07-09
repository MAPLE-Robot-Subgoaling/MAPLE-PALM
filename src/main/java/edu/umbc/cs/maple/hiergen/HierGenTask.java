package edu.umbc.cs.maple.hiergen;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class HierGenTask {
    Map<Object, Object> goal;
    ArrayList<String> actions;
    ArrayList<Object> variables;
    ArrayList<HierGenTask> subTasks;


    public HierGenTask() {
        goal = null;
        actions = new ArrayList<String>();
        variables = new ArrayList<Object>();
    }

    public HierGenTask(Map<Object, Object> g, ArrayList<String> a, ArrayList<Object> v) {
        goal = g;
        actions = a;
        variables = v;
        subTasks = null;
    }

    public HierGenTask(Map<Object, Object> g, ArrayList<String> a, ArrayList<Object> v, ArrayList<HierGenTask> subTasks) {
        goal = g;
        actions = a;
        variables = v;
        this.subTasks = subTasks;
    }

    public String toString() {

        return "\n--------------Task----------\n" +
                actions.toString() + variables.toString();
    }

    public String actionName() {
        String action = "";
        for (Object var : variables)
            action += var + " ";
        return action + "\n";
    }
}
