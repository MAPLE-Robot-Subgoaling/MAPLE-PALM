package hiergen;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.action.Action;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by khalil8500 on 3/25/2018.
 */
public class Task {
    Map<Object, Object> goal;
    ArrayList<Object> actions;
    ArrayList<String> variables;
    ArrayList<Task> subTasks;


    public Task()
    {
        goal = null;
        actions = new ArrayList<Object>();
        variables = new ArrayList<String>();
    }

    public Task(Map<Object, Object> g, ArrayList<Object> a, ArrayList<String> v)
    {
        goal = g;
        actions = a;
        variables = v;
        subTasks = null;
    }

    public Task(Map<Object, Object> g, ArrayList<Object> a, ArrayList<String> v, ArrayList<Task> subTasks)
    {
        goal = g;
        actions = a;
        variables = v;
        this.subTasks = subTasks;
    }

    public String toString()
    {
        return "\n--------------Task----------\n" +
                actions.toString();
    }

    public String actionName()
    {
        String action = "";
        for(String var: variables)
            action += "var ";
        return action + "\n";
    }
}
