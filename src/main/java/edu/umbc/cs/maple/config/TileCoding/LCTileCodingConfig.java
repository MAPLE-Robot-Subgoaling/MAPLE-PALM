package edu.umbc.cs.maple.config.TileCoding;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement;

import static edu.umbc.cs.maple.liftCopter.LiftCopterConstants.*;


public class LCTileCodingConfig implements TileCodingConfig {
    public double res = 1;
    public DifferentiableStateActionValue generateVFA(){
        ConcatenatedObjectFeatures inputFeatures = new ConcatenatedObjectFeatures()
                .addObjectVectorizion(CLASS_AGENT, new NumericVariableFeatures()
                        .addToWhiteList(ATT_X)
                        .addToWhiteList(ATT_Y)
                        .addToWhiteList(ATT_VX)
                        .addToWhiteList(ATT_VY)
                        .addToWhiteList(ATT_H)
                        .addToWhiteList(ATT_W))
                .addObjectVectorizion(CLASS_LOCATION, new NumericVariableFeatures()
                        .addToWhiteList(ATT_X)
                        .addToWhiteList(ATT_Y)
                        .addToWhiteList(ATT_H)
                        .addToWhiteList(ATT_W))
                .addObjectVectorizion(CLASS_WALL, new NumericVariableFeatures()
                        .addToWhiteList(ATT_START_X)
                        .addToWhiteList(ATT_START_Y)
                        .addToWhiteList(ATT_HEIGHT)
                        .addToWhiteList(ATT_WIDTH))
                ;

        int nTilings = 50;

        double agentXWidth = 1 / res;
        double agentYWidth = 3/ res;
        double agentXVelocityWidth = 2 * PHYS_MAX_VX / res;
        double agentYVelocityWidth = 2 * PHYS_MAX_VY / res;
        double agentHeightWidth = 1/res;
        double agentWidthWidth = 1/res;

        double location1XWidth = 1 / res;
        double location1YWidth = 3 / res;
        double location1HeightWidth = 1/res;
        double location1WidthWidth = 1/res;

        double location2XWidth = 1 / res;
        double location2YWidth = 3 / res;
        double location2HeightWidth = 1/res;
        double location2WidthWidth = 1/res;

        double location3XWidth = 1 / res;
        double location3YWidth = 3 / res;
        double location3HeightWidth = 1/res;
        double location3WidthWidth = 1/res;

        double location4XWidth = 1 / res;
        double location4YWidth = 3 / res;
        double location4HeightWidth = 1/res;
        double location4WidthWidth = 1/res;

        double wall1XWidth = 1 / res;
        double wall1YWidth = 3 / res;
        double wall1HeightWidth = 1/res;
        double wall1WidthWidth = 1/res;

        double wall2XWidth = 1 / res;
        double wall2YWidth = 3 / res;
        double wall2HeightWidth = 1/res;
        double wall2WidthWidth = 1/res;

        double wall3XWidth = 1 / res;
        double wall3YWidth = 3 / res;
        double wall3HeightWidth = 1/res;
        double wall3WidthWidth = 1/res;

        double wall4XWidth = 1 / res;
        double wall4YWidth = 3 / res;
        double wall4HeightWidth = 1/res;
        double wall4WidthWidth = 1/res;

        double wall5XWidth = 1 / res;
        double wall5YWidth = 3 / res;
        double wall5HeightWidth = 1/res;
        double wall5WidthWidth = 1/res;

        double wall6XWidth = 1 / res;
        double wall6YWidth = 3 / res;
        double wall6HeightWidth = 1/res;
        double wall6WidthWidth = 1/res;

        double wall7XWidth = 1 / res;
        double wall7YWidth = 3 / res;
        double wall7HeightWidth = 1/res;
        double wall7WidthWidth = 1/res;

        TileCodingFeatures tilecoding = new TileCodingFeatures(inputFeatures);
        tilecoding.addTilingsForAllDimensionsWithWidths(
                new double[]{
                        agentXWidth,
                        agentYWidth,
                        agentXVelocityWidth,
                        agentYVelocityWidth,
                        agentHeightWidth,
                        agentWidthWidth,
                        location1XWidth,
                        location1YWidth,
                        location1HeightWidth,
                        location1WidthWidth,
                        location2XWidth,
                        location2YWidth,
                        location2HeightWidth,
                        location2WidthWidth,
                        location3XWidth,
                        location3YWidth,
                        location3HeightWidth,
                        location3WidthWidth,
                        location4XWidth,
                        location4YWidth,
                        location4HeightWidth,
                        location4WidthWidth,
                        wall1XWidth,
                        wall1YWidth,
                        wall1HeightWidth,
                        wall1WidthWidth,
                        wall2XWidth,
                        wall2YWidth,
                        wall2HeightWidth,
                        wall2WidthWidth,
                        wall3XWidth,
                        wall3YWidth,
                        wall3HeightWidth,
                        wall3WidthWidth,
                        wall4XWidth,
                        wall4YWidth,
                        wall4HeightWidth,
                        wall4WidthWidth,
                        wall5XWidth,
                        wall5YWidth,
                        wall5HeightWidth,
                        wall5WidthWidth,
                        wall6XWidth,
                        wall6YWidth,
                        wall6HeightWidth,
                        wall6WidthWidth,
                        wall7XWidth,
                        wall7YWidth,
                        wall7HeightWidth,
                        wall7WidthWidth
                },
                nTilings,
                TilingArrangement.RANDOM_JITTER);




        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = tilecoding.generateVFA(defaultQ/nTilings);
        return vfa;
    }

}
