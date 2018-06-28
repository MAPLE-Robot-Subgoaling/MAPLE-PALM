package edu.umbc.cs.maple.palm.agent;

import edu.umbc.cs.maple.hierarchy.framework.GroundedTask;
import edu.umbc.cs.maple.hierarchy.framework.Task;

public interface PALMModelGenerator {

    PALMModel getModelForTask(Task t);

}
