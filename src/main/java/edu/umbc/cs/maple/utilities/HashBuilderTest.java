package edu.umbc.cs.maple.utilities;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

public class HashBuilderTest {

    public static void main(String[] args) {

        int [] hashCodesFirst   = new int[]{
            212522630,
            1816523798,
            1816521876,
            -1264514673,
            -1168921576,
            -1267429914,
            -1412481568,
            -1412482597,
            -1412449855,
            -1412477792
        };
        int [] hashCodesSecond  = new int[] {
                212522631,
                1816522837,
                1816521876,
                -1264514673,
                -1168921576,
                -1267429914,
                -1412481568,
                -1412482597,
                -1412449855,
                -1412477792
        };

        Arrays.sort(hashCodesFirst);
        HashCodeBuilder hashCodeBuilderOne = new HashCodeBuilder(17, 31);
        for (int i : hashCodesFirst) {
            int hc = hashCodeBuilderOne.append(i).toHashCode();
            System.out.print(hc + " ");
        }
        System.out.println("");
//        hashCodeBuilderOne.append(hashCodesFirst);
        int first = hashCodeBuilderOne.toHashCode();

        Arrays.sort(hashCodesSecond);
        HashCodeBuilder hashCodeBuilderTwo = new HashCodeBuilder(17, 31);
        for (int i : hashCodesSecond) {
            int hc = hashCodeBuilderTwo.append(i).toHashCode();
            System.out.print(hc + " ");
        }
        System.out.println("");
//        hashCodeBuilderTwo.append(hashCodesSecond);
        int second = hashCodeBuilderTwo.toHashCode();

        System.out.println(first);
        System.out.println(second);
    }
}
