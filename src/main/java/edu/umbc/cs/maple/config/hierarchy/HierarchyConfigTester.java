package edu.umbc.cs.maple.config.hierarchy;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;

public class HierarchyConfigTester {
    public static void main(String[] args) {
        Constructor constructor = new Constructor(HierarchyConfig.class);
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(false);
        Yaml yaml = new Yaml(constructor, representer);
        InputStream input = ClassLoader.getSystemResourceAsStream("config/hierarchy/taxi-expert.yaml");
        HierarchyConfig config = (HierarchyConfig) yaml.load(input);
        config.buildRoot();
    }
}
