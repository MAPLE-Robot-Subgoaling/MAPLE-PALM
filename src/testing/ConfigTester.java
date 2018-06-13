package testing;

import config.ExperimentConfig;

import java.io.File;

public class ConfigTester {

    public static void main(String[] args) {
        String command = args[0];
        if (command.equals("ALL-TAXI")) {
            String configPathTaxi = "./config/taxi/";
            File folder = new File(configPathTaxi);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        String name = listOfFile.getName();
                        String configFile = configPathTaxi + name;
                        System.out.println("\n\n*************************\nINITIALIZING: " + configFile +"\n*************************\n\n");
                        ExperimentConfig config = ExperimentConfig.loadConfig(configFile);
                        // turn off visualization
                        config.output.chart.enabled = false;
                        config.output.visualizer.episodes = false;
                        config.output.visualizer.enabled = false;
                        HierarchicalCharts.run(config);
                    }
                }
            }
            System.exit(33);
        }
    }
}
