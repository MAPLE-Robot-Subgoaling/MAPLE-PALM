package edu.umbc.cs.maple.variableobjecttilecoding;

public class VOTiling {
    public double[] widths;
    protected double[] offset;
    protected boolean[] dimensionMask;

    public VOTiling(double[] widths, double[] offset) {
        this.widths = (double[])widths.clone();
        this.offset = (double[])offset.clone();
        this.dimensionMask = new boolean[this.widths.length];

        for(int i = 0; i < this.dimensionMask.length; ++i) {
            this.dimensionMask[i] = true;
        }

    }

    public VOTiling(double[] widths, double[] offset, boolean[] dimensionMask) {
        this.widths = (double[])widths.clone();
        this.offset = (double[])offset.clone();
        this.dimensionMask = (boolean[])dimensionMask.clone();
    }

    public VOTiling.FVTile getFVTile(double[] input) {
        if (input.length != this.widths.length) {
           // throw new RuntimeException("Error: the input feature vector to be tiled is a different dimensionality than the dimensionality on which this tiling was defined; e.g., the specified widths vector for this tiling is a different dimension than the input vector.");
            VOTiling.FVTile tile = new VOTiling.FVTile(new int[this.widths.length]);
            return tile;
        } else {
            int[] tiledVector = new int[input.length];

            for(int i = 0; i < input.length; ++i) {
                if (this.dimensionMask[i]) {
                    tiledVector[i] = (int)Math.floor((input[i] - this.offset[i]) / this.widths[i]);
                } else {
                    tiledVector[i] = 0;
                }
            }

            VOTiling.FVTile tile = new VOTiling.FVTile(tiledVector);
            return tile;
        }
    }

    public class FVTile {
        public int[] tiledVector;
        protected int hashCode;

        public FVTile(int[] tiledVector) {
            this.tiledVector = tiledVector;
            this.hashCode = 0;

            for(int i = 0; i < tiledVector.length; ++i) {
                if (VOTiling.this.dimensionMask[i]) {
                    this.hashCode = 31 * this.hashCode + tiledVector[i];
                }
            }

        }

        public boolean equals(Object other) {
            if (!(other instanceof VOTiling.FVTile)) {
                return false;
            } else {
                VOTiling.FVTile o = (VOTiling.FVTile)other;
                if (this.tiledVector.length != o.tiledVector.length) {
                    return false;
                } else {
                    for(int i = 0; i < this.tiledVector.length; ++i) {
                        if (this.tiledVector[i] != o.tiledVector[i]) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }

        public int hashCode() {
            return this.hashCode;
        }
    }
}
