package edu.umbc.cs.maple.utilities;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HashBuilderTest2 {

    public static void main(String[] args) {

        List<Object> stateOne = new ArrayList<>();
        stateOne.add(0.10000000000000002);
        stateOne.add(2.2300000000000004);
        stateOne.add(-8.572527594031472E-18);
        stateOne.add(-0.02);
//        stateOne.add(0.5);
//        stateOne.add(0.5);

        List<Object> stateTwo = new ArrayList<>();
        stateTwo.add(0.22999999999999965);
        stateTwo.add(2.300000000000002);
        stateTwo.add(-0.020000000000000007);
        stateTwo.add(0.04000000000000001);
//        stateTwo.add(0.5);
//        stateTwo.add(0.5);

        HashCodeBuilder hashCodeBuilderOne = new HashCodeBuilder(41, 89);
        for (Object value : stateOne) {
            hashCodeBuilderOne.append(value);
        }
        HashCodeBuilder hashCodeBuilderTwo = new HashCodeBuilder(41, 89);
        for (Object value : stateTwo) {
            hashCodeBuilderTwo.append(value);
        }

        String first = "" + hashCodeBuilderOne.toHashCode();
        String second = "" + hashCodeBuilderTwo.toHashCode();

        System.out.println(first);
        System.out.println(second);
    }
}
