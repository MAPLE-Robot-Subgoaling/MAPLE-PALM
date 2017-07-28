package action.models;

import java.io.File;
import java.nio.file.Files;

public class VariableTree {

    public VariableTree(String tree){
        read(tree);
    }

    public VariableTree(File tree){
        read(tree);
    }

    protected void read(File file){
        try {
            String tree = new String(Files.readAllBytes(file.toPath()));
//            System.out.println(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected void read(String tree){

    }
}
