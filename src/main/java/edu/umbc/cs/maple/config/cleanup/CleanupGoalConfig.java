package edu.umbc.cs.maple.config.cleanup;

import edu.umbc.cs.maple.cleanup.CleanupGoalDescription;

public class CleanupGoalConfig{
    public CleanupGoalDescription[] goalDescriptions;

    public CleanupGoalConfig(){}

    public CleanupGoalDescription[] getGoalDescriptions(){return goalDescriptions;}

    public void setGoalDescriptions(CleanupGoalDescription[] d){
        goalDescriptions=d;
    }

}
