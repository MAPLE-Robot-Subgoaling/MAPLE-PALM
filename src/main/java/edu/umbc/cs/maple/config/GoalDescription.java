package edu.umbc.cs.maple.config;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import org.apache.commons.lang3.StringUtils;

public class GoalDescription {
    protected String[] params;
    protected PropositionalFunction pf;

    public GoalDescription(){

    }
    public GoalDescription(String[] params, PropositionalFunction pf) {
        this.params = params;
        this.pf = pf;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public PropositionalFunction getPf() {
        return pf;
    }

    public void setPf(PropositionalFunction pf) {
        this.pf = pf;
    }

    public String toString() {
        return "" + pf.getName() + "<" + StringUtils.join(params, "|") + ">";
    }
}
