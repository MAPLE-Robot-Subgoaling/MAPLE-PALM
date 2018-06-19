package edu.umbc.cs.maple.testing;

import edu.umbc.cs.maple.config.ExperimentConfig;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class ConfigTester {

    public static void run(String configFile) {
        System.out.println("\n\n*************************\nINITIALIZING: " + configFile +"\n*************************\n\n");
        ExperimentConfig config = ExperimentConfig.loadConfig(configFile);
        // turn off visualization
        config.output.chart.enabled = false;
        config.output.visualizer.episodes = false;
        config.output.visualizer.enabled = false;
        HierarchicalCharts.run(config);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("\n\nPlease pass in a config file, or command (one of): " + Arrays.toString(ConfigCommand.values()));
            System.exit(-1);
        }
        String command = args[0];
        if (Stream.of(ConfigCommand.values()).anyMatch(x -> x.name().equals(command))) {
            ConfigCommand configCommand = ConfigCommand.valueOf(command);
            String configPath = configCommand.directory;
            File folder = new File(configPath);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        String name = listOfFile.getName();
                        String configFile = configPath + name;
                        run(configFile);
                    }
                }
            }
        } else {
            String configFile = command;
            run(configFile);
        }
    }

    public enum ConfigCommand {
        ALL_TAXI("./config/taxi/"),
        ALL_CLEANUP("./config/cleanup/"),
        ;

        public String directory;

        ConfigCommand(String directory) {
            this.directory = directory;
        }

    }

}
