package edu.umbc.cs.maple.VariableObjectTileCoding;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures;
import burlap.behavior.functionapproximation.sparse.tilecoding.Tiling;
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.behavior.functionapproximation.sparse.tilecoding.Tiling.FVTile;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.*;

public class VariableObjectTileCoding implements SparseStateFeatures {
    public VariableObjectTileCoding(DenseStateFeatures featureVectorGenerator) {
        this.featureVectorGenerator = featureVectorGenerator;
        this.tilings = new ArrayList();
        this.stateFeatures = new ArrayList();

    }

    protected DenseStateFeatures featureVectorGenerator;
    protected Random rand = RandomFactory.getMapped(0);
    List<VOTiling> tilings;
    List<Map<VOTiling.FVTile, Integer>> stateFeatures;
    protected int nextStateFeatureId = 0;

    @Override
    public VariableObjectTileCoding copy() {
        VariableObjectTileCoding tilecoding = new VariableObjectTileCoding(this.featureVectorGenerator);
        tilecoding.rand = this.rand;
        tilecoding.tilings = new ArrayList(this.tilings);
        tilecoding.stateFeatures = new ArrayList(this.stateFeatures.size());
        Iterator var2 = this.stateFeatures.iterator();

        while(var2.hasNext()) {
            Map<VOTiling.FVTile, Integer> el = (Map)var2.next();
            Map<VOTiling.FVTile, Integer> nel = new HashMap(el);
            tilecoding.stateFeatures.add(nel);
        }

        tilecoding.nextStateFeatureId = this.nextStateFeatureId;
        return tilecoding;
    }

    public void addTilingsForDimensionsAndWidths(boolean[] dimensionMask, double[] widths, int nTilings, TilingArrangement tileArrangement) {
        for(int i = 0; i < nTilings; ++i) {
            this.stateFeatures.add(new HashMap());
            double[] offset;
            if (tileArrangement == TilingArrangement.RANDOM_JITTER) {
                offset = this.produceRandomOffset(dimensionMask, widths);
            } else {
                offset = this.produceUniformTilingsOffset(dimensionMask, widths, i, nTilings);
            }

            VOTiling tiling = new VOTiling(widths, offset, dimensionMask);
            this.tilings.add(tiling);
        }

    }

    public void addTilingsForAllDimensionsWithWidths(double[] widths, int nTilings, TilingArrangement tileArrangement) {
        boolean[] dimensionMask = new boolean[widths.length];

        for(int i = 0; i < dimensionMask.length; ++i) {
            dimensionMask[i] = true;
        }

        this.addTilingsForDimensionsAndWidths(dimensionMask, widths, nTilings, tileArrangement);
    }

    @Override
    public List<StateFeature> features(State s) {
        double[] input = this.featureVectorGenerator.features(s);
        List<StateFeature> features = new ArrayList();

        for(int i = 0; i < this.tilings.size(); ++i) {
            VOTiling tiling = (VOTiling)this.tilings.get(i);
            Map<VOTiling.FVTile, Integer> tileFeatureMap = (Map)this.stateFeatures.get(i);


            VOTiling.FVTile tile = tiling.getFVTile(input);

            int f = this.getOrGenerateFeature(tileFeatureMap, tile);
            StateFeature sf = new StateFeature(f, 1.0D);
            features.add(sf);
        }

        return features;
    }

    @Override
    public int numFeatures() {
        return this.nextStateFeatureId;
    }


    protected int getOrGenerateFeature(Map<VOTiling.FVTile, Integer> tileFeatureMap, VOTiling.FVTile tile) {
        Integer stored = (Integer)tileFeatureMap.get(tile);
        if (stored == null) {
            stored = this.nextStateFeatureId;
            tileFeatureMap.put(tile, stored);
            ++this.nextStateFeatureId;
        }

        return stored;
    }

    protected List<VariableObjectTileCoding.ActionFeatureID> getOrGenerateActionFeatureList(Map<Tiling.FVTile, List<VariableObjectTileCoding.ActionFeatureID>> tileFeatureMap, Tiling.FVTile tile) {
        List<VariableObjectTileCoding.ActionFeatureID> stored = (List)tileFeatureMap.get(tile);
        if (stored == null) {
            stored = new ArrayList();
            tileFeatureMap.put(tile, stored);
        }

        return (List)stored;
    }

    public LinearVFA generateVFA(double defaultWeightValue) {
        return new LinearVFA(this, defaultWeightValue);
    }

    protected double[] produceRandomOffset(boolean[] dimensionMask, double[] widths) {
        double[] offset = new double[dimensionMask.length];

        for(int i = 0; i < offset.length; ++i) {
            if (dimensionMask[i]) {
                offset[i] = this.rand.nextDouble() * widths[i];
            } else {
                offset[i] = 0.0D;
            }
        }

        return offset;
    }

    protected double[] produceUniformTilingsOffset(boolean[] dimensionMask, double[] widths, int ithTiling, int nTilings) {
        double[] offset = new double[dimensionMask.length];

        for(int i = 0; i < offset.length; ++i) {
            if (dimensionMask[i]) {
                offset[i] = (double)ithTiling / (double)nTilings * widths[i];
            } else {
                offset[i] = 0.0D;
            }
        }

        return offset;
    }

    protected VariableObjectTileCoding.ActionFeatureID matchingActionFeature(List<VariableObjectTileCoding.ActionFeatureID> actionFeatures, Action forAction) {
        Iterator var3 = actionFeatures.iterator();

        VariableObjectTileCoding.ActionFeatureID aid;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            aid = (VariableObjectTileCoding.ActionFeatureID)var3.next();
        } while(!aid.ga.equals(forAction));

        return aid;
    }

    protected class ActionFeatureID {
        public int id;
        public Action ga;

        public ActionFeatureID(Action ga, int id) {
            this.id = id;
            this.ga = ga;
        }
    }
}


