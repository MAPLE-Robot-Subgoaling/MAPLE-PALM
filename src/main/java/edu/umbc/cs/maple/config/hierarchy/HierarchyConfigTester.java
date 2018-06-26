package edu.umbc.cs.maple.config.hierarchy;

import burlap.mdp.auxiliary.DomainGenerator;
import edu.umbc.cs.maple.hierarchy.framework.Hierarchy;
import edu.umbc.cs.maple.hierarchy.framework.Task;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;

public class HierarchyConfigTester {
    public static void main(String[] args) {
        Constructor constructor = new Constructor(HierarchyConfig.class);

        TypeDescription typeHierarchyConfig = new TypeDescription(HierarchyConfig.class);
        typeHierarchyConfig.putMapPropertyType("hierarchyConfigMap", String.class, TaskConfig.class);
        constructor.addTypeDescription(typeHierarchyConfig);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(constructor, representer);
        InputStream input = ClassLoader.getSystemResourceAsStream("config/hierarchy/taxi-hiergen.yaml");
        HierarchyConfig config = (HierarchyConfig) yaml.load(input);
        Task root = config.buildRoot();
        System.out.println("done");
    }
}
