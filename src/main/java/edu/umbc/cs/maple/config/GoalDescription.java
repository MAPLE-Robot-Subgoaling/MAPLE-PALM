package edu.umbc.cs.maple.config;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import org.apache.commons.lang3.StringUtils;

import static edu.umbc.cs.maple.utilities.BurlapConstants.EMPTY_ARRAY;

public class GoalDescription {

    protected static final String SEPERATOR = "|";
    protected static final String DELIMITER_LEFT = "<";
    protected static final String DELIMITER_RIGHT = ">";

    protected String[] params;
    protected PropositionalFunction pf;

    public GoalDescription(){
        this(EMPTY_ARRAY, null);
    }

    public GoalDescription(PropositionalFunction pf){
        this(EMPTY_ARRAY, pf);
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
        return pf == null ? "ERROR: pf not set" :
                pf.getName() + DELIMITER_LEFT + StringUtils.join(params, SEPERATOR) + DELIMITER_RIGHT;
    }
}
