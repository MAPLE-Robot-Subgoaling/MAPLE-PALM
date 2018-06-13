package testing;

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
                        String config = configPathTaxi + name;
                        System.out.println("\n\n*************************\nINITIALIZING: " + config +"\n*************************\n\n");
                        HierarchicalCharts.main(new String[]{config});
                    }
                }
            }
            System.exit(33);
        }
    }
}
