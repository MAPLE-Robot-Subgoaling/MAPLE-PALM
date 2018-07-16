package edu.umbc.cs.maple.config.TileCoding;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;

public interface TileCodingConfig {
    public double res = 0;
    public DifferentiableStateActionValue generateVFA();
}
