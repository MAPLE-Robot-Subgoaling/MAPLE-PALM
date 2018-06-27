package edu.umbc.cs.maple.config.taxi;

import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.DomainConfig;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.taxi.TaxiVisualizer;
import edu.umbc.cs.maple.taxi.state.TaxiState;
import edu.umbc.cs.maple.taxi.stategenerator.TaxiStateFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaxiConfig extends DomainConfig {
    public double correct_move;
    public double fickle;

    public TaxiState generateState() {
        String passengerNumberRegex = "\\-(\\d+)passengers";
        Pattern r = Pattern.compile(passengerNumberRegex);
        Matcher m = r.matcher(state);
        String numPassengersString = "";
        if (m.find()) {
            numPassengersString = m.group(1);
        }
        int numPassengers = "".equals(numPassengersString) ? 1 : Integer.parseInt(numPassengersString);
        if        (state.equals("classic")) {
            return TaxiStateFactory.createClassicState();
        } else if (state.equals("tiny")) {
            return TaxiStateFactory.createTinyState();
        } else if (state.equals("small")) {
            return TaxiStateFactory.createSmallState();
        } else if (state.equals("medium")) {
            return TaxiStateFactory.createMediumState();
        } else if (state.equals("3depots")) {
            return TaxiStateFactory.createThreeDepots();
        } else if (state.equals("mehta-zigzag-1")) {
            return TaxiStateFactory.createMehtaZigZag1State(1);
        } else if (state.equals("mehta-zigzag-2")) {
            return TaxiStateFactory.createMehtaZigZag2State(1);
        } else if (state.equals("steptest")) {
            return TaxiStateFactory.createStepTest(1);
        } else if (state.matches("classic" + passengerNumberRegex)) {
            return TaxiStateFactory.createClassicState(numPassengers);
        } else if (state.matches("classic20" + passengerNumberRegex)) {
            return TaxiStateFactory.createClassic20State(numPassengers);
        } else if (state.matches("tiny" + passengerNumberRegex)) {
            return TaxiStateFactory.createTinyState(numPassengers);
        } else if (state.matches("tiny3" + passengerNumberRegex)) {
            return TaxiStateFactory.createTiny3State(numPassengers);
        } else if (state.matches("small" + passengerNumberRegex)) {
            return TaxiStateFactory.createSmallState(numPassengers);
        } else if (state.matches("medium" + passengerNumberRegex)) {
            return TaxiStateFactory.createMediumState(numPassengers);
        } else if (state.matches("3depots" + passengerNumberRegex)) {
            return TaxiStateFactory.createThreeDepots(numPassengers);
        } else if (state.matches("mehta-zigzag-1" + passengerNumberRegex)) {
            return TaxiStateFactory.createMehtaZigZag1State(numPassengers);
        } else if (state.matches("mehta-zigzag-2" + passengerNumberRegex)) {
            return TaxiStateFactory.createMehtaZigZag2State(numPassengers);
        } else if (state.matches("steptest" + passengerNumberRegex)) {
            return TaxiStateFactory.createStepTest(numPassengers);
        } else if (state.matches("discounttest" + passengerNumberRegex)) {
            return TaxiStateFactory.createDiscountTest(numPassengers);
        } else if (state.matches("discounttestbig" + passengerNumberRegex)) {
            return TaxiStateFactory.createDiscountTestBig(numPassengers);
        } else {
            throw new RuntimeException("ERROR: invalid state passed to generateState in TaxiConfig: " + state);
        }
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return TaxiVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }
}
