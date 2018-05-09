package palm.agent;

import hierarchy.framework.GroundedTask;

public interface PALMModelGenerator {

    public PALMModel getModelForTask(GroundedTask t);
}
