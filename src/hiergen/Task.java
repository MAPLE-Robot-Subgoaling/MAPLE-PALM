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
    }

    public String toString()
    {
        return "\n--------------Task----------\n" +
                actions.toString();
    }
}
