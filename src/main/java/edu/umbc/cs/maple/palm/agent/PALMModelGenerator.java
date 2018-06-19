package edu.umbc.cs.maple.palm.agent;

import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;

public interface PALMModelGenerator {

    public PALMModel getModelForTask(GroundedTask t);
}
