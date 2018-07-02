package edu.umbc.cs.maple.config.hierarchy;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import edu.umbc.cs.maple.config.ExperimentConfig;
import edu.umbc.cs.maple.hierarchy.framework.PrimitiveTask;
import edu.umbc.cs.maple.hierarchy.framework.SolveActionType;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HierarchyConfig {
    public String name;
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
                ActionType childActionType = homeDomain.getAction(childTaskName.split("_")[0]);
                if(childTaskName.endsWith("_np")){
                    childTasksList.add(buildTask(childTaskName.split("_")[0], childActionType));
                }else if(childTaskName.endsWith("_p")){
                 //   childActionType = ((SADomain)baseDomain).getAction(childTaskName.split("_")[0]);
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
    public boolean isDoPlanOnly() {
        return doPlanOnly;
    }
    public void setDoPlanOnly(boolean doPlanOnly) {
        this.doPlanOnly = doPlanOnly;
    }
    public Task getRoot(){
        if(!(taskMap.keySet().contains("root"))){
            return buildRoot();
        }else{
            return taskMap.get("root");
        }
    }

    public static HierarchyConfig load(ExperimentConfig experimentConfig, String configPath){
        Constructor constructor = new Constructor(HierarchyConfig.class);

        TypeDescription typeHierarchyConfig = new TypeDescription(HierarchyConfig.class);
        typeHierarchyConfig.putMapPropertyType("hierarchyConfigMap", String.class, TaskConfig.class);
        constructor.addTypeDescription(typeHierarchyConfig);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(constructor, representer);

        InputStream input = ClassLoader.getSystemResourceAsStream(configPath);
        HierarchyConfig config = (HierarchyConfig) yaml.load(input);
        config.baseDomainGenerator = experimentConfig.domain.getDomainGenerator();
        config.buildRoot();
        return config;
    }


}
