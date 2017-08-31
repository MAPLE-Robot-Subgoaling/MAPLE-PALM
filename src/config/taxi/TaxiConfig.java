package config.taxi;

import config.output.OutputConfig;
import config.rmax.RmaxConfig;

import java.util.List;

public class TaxiConfig {
    public String state;
    public List<String> agents;
    public int episodes;
    public int max_steps;
    public int trials;
    public double gamma;

    public StochasticTaxiConfig stochastic;
    public RmaxConfig rmax;
    public OutputConfig output;
}
