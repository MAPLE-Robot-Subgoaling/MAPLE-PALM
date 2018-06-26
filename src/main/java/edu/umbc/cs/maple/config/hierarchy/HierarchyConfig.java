package edu.umbc.cs.maple.config.hierarchy;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
import edu.umbc.cs.maple.hierarchy.framework.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HierarchyConfig {
    private Map<String,TaskConfig> hierarchyConfigMap;
    private Map<String,Task> taskMap;
    private DomainGenerator baseDomainGenerator;
    private Domain baseDomain;
    boolean doPlanOnly = false; // set up handling for planning only

    public HierarchyConfig(){}

    public Task buildRoot(){
        taskMap = new HashMap<String,Task>();
        baseDomain = baseDomainGenerator.generateDomain();
        Task root = buildTask("root", new SolveActionType());
        return root;
    }

    public Task buildTask(String taskName, ActionType actionType){
        if(taskMap.containsKey(taskName)){
            return taskMap.get(taskName);
        }
        if(hierarchyConfigMap.containsKey(taskName)){
            TaskConfig taskConfig = hierarchyConfigMap.get(taskName);
            OOSADomain homeDomain = (OOSADomain) taskConfig.buildDomain();
            List<Task> childTasksList = new ArrayList<Task>();
            for (String childTaskName: taskConfig.getChildren()) {
                System.out.println(childTaskName.split("_")[0]);
                ActionType childActionType = homeDomain.getAction(childTaskName.split("_")[0]);
                if(childTaskName.endsWith("_np")){
                    childTasksList.add(buildTask(childTaskName.split("_")[0], childActionType));
                }else if(childTaskName.endsWith("_p")){
                    childActionType = ((SADomain)baseDomain).getAction(childTaskName.split("_")[0]);
                    childTasksList.add(new PrimitiveTask(childActionType, ((OOSADomain)baseDomain)));
                }
            }
            Task finalizedTask = taskConfig.finalizeTask(childTasksList, actionType);
            taskMap.put(taskName, finalizedTask);
            return finalizedTask;
        }
        throw new IllegalArgumentException("task is not present in Config map");
    }

    public Map<String, TaskConfig> getHierarchyConfigMap() {
        return hierarchyConfigMap;
    }
    public void setHierarchyConfigMap(Map<String, TaskConfig> hierarchyConfigMap) {
        this.hierarchyConfigMap = hierarchyConfigMap;
    }
    public DomainGenerator getBaseDomainGenerator() {
        return baseDomainGenerator;
    }
    public void setBaseDomainGenerator(DomainGenerator baseDomainGenerator) {
        this.baseDomainGenerator = baseDomainGenerator;
    }
    public boolean isDoPlanOnly() {
        return doPlanOnly;
    }
    public void setDoPlanOnly(boolean doPlanOnly) {
        this.doPlanOnly = doPlanOnly;
    }
}
