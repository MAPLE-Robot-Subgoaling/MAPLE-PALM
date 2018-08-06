package edu.umbc.cs.maple.config.liftcopter;

import burlap.visualizer.Visualizer;
import edu.umbc.cs.maple.config.DomainConfig;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.liftcopter.LiftCopterVisualizer;
import edu.umbc.cs.maple.liftcopter.state.LiftCopterState;
import edu.umbc.cs.maple.liftcopter.stategenerator.LiftCopterStateFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LCConfig extends DomainConfig {
    public double correct_move;
    public double fickle;

    public LiftCopterState generateState() {
        String passengerNumberRegex = "\\-(\\d+)passengers";
        Pattern r = Pattern.compile(passengerNumberRegex);
        Matcher m = r.matcher(state);
        String numPassengersString = "";
        if (m.find()) {
            numPassengersString = m.group(1);
        }
        int numPassengers = "".equals(numPassengersString) ? 1 : Integer.parseInt(numPassengersString);
        if        (state.equals("classic")) {
            return LiftCopterStateFactory.createClassicState();
        } else {
            throw new RuntimeException("ERROR: invalid state passed to generateState in TaxiConfig: " + state);
        }
    }

    @Override
    public Visualizer getVisualizer(ExperimentConfig config) {
        return LiftCopterVisualizer.getVisualizer(config.output.visualizer.width, config.output.visualizer.height);
    }
}
