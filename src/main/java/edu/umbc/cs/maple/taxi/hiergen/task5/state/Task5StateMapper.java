package edu.umbc.cs.maple.taxi.hiergen.task5.state;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;
import edu.umbc.cs.maple.taxi.state.TaxiState;

import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_X;
import static edu.umbc.cs.maple.taxi.TaxiConstants.ATT_Y;

public class Task5StateMapper implements StateMapping {
    @Override
    public State mapState(State s) {
        TaxiState st = (TaxiState) s;

        int tx = (int) st.getTaxi().get(ATT_X);
        int ty = (int) st.getTaxi().get(ATT_Y);
        TaxiHierGenTask5Taxi taxi = new TaxiHierGenTask5Taxi(st.getTaxiName(), tx, ty);

        return new TaxiHierGenTask5State(taxi);
    }
}
