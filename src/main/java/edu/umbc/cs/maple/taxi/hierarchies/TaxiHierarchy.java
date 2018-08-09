//package edu.umbc.cs.maple.taxi.hierarchies;
//
//import edu.umbc.cs.maple.config.ExperimentConfig;
//import edu.umbc.cs.maple.config.taxi.TaxiConfig;
//import edu.umbc.cs.maple.hierarchy.framework.Hierarchy;
//import edu.umbc.cs.maple.hierarchy.framework.Task;
//
//public abstract class TaxiHierarchy extends Hierarchy {
//
//    @Override
//    public Task createHierarchy(ExperimentConfig experimentConfig, boolean plan) {
//        TaxiConfig domain = (TaxiConfig) experimentConfig.domain;
//        return createHierarchy(plan);
//    }
//
//    public abstract Task createHierarchy(double correctMoveprob, double fickleProbability, boolean plan);
//
//}