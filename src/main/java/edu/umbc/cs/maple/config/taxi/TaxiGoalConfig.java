package edu.umbc.cs.maple.config.taxi;

import burlap.mdp.core.oo.propositional.PropositionalFunction;

public class TaxiGoalConfig {
    public PropositionalFunction pf;

    public TaxiGoalConfig(){

    }

    public PropositionalFunction getPf(){
        return pf;
    }

    public void setPf(PropositionalFunction pf){
        this.pf=pf;
    }
}
