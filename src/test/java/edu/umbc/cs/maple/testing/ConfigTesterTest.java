package edu.umbc.cs.maple.testing;

import org.junit.Test;

public class ConfigTesterTest {

    @Test
    public void run() throws Exception {
        String input = "config/taxi/small.yaml";
        ConfigTester.run(input);
    }

//    @Test
//    public void main() throws Exception {
//
//    }

}