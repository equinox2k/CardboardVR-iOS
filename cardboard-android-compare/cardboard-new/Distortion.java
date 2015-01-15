package com.google.vrtoolkit.cardboard;

import java.util.*;

public class Distortion
{
    private static final float[] DEFAULT_COEFFICIENTS;
    private float[] mCoefficients;
    
    public Distortion() {
        super();
        this.mCoefficients = Distortion.DEFAULT_COEFFICIENTS.clone();
    }
    
    public Distortion(final Distortion other) {
        super();
        this.setCoefficients(other.mCoefficients);
    }
    
    public static Distortion parseFromProtobuf(final float[] coefficients) {
        final Distortion distortion = new Distortion();
        distortion.setCoefficients(coefficients);
        return distortion;
    }
    
    public float[] toProtobuf() {
        return this.mCoefficients.clone();
    }
    
    public void setCoefficients(final float[] coefficients) {
        this.mCoefficients = ((coefficients != null) ? coefficients.clone() : new float[0]);
    }
    
    public float[] getCoefficients() {
        return this.mCoefficients;
    }
    
    public float distortionFactor(final float radius) {
        float result = 1.0f;
        float rFactor = 1.0f;
        final float rSquared = radius * radius;
        for (final float ki : this.mCoefficients) {
            rFactor *= rSquared;
            result += ki * rFactor;
        }
        return result;
    }
    
    public float distort(final float radius) {
        return radius * this.distortionFactor(radius);
    }
    
    public float distortInverse(final float radius) {
        float r0 = radius / 0.9f;
        float r = radius * 0.9f;
        float dr0 = radius - this.distort(r0);
        while (Math.abs(r - r0) > 1.0E-4) {
            final float dr = radius - this.distort(r);
            final float r2 = r - dr * ((r - r0) / (dr - dr0));
            r0 = r;
            r = r2;
            dr0 = dr;
        }
        return r;
    }
    
    private static double[] solveLeastSquares(final double[][] matA, final double[] vecY) {
        final int numSamples = matA.length;
        final int numCoefficients = matA[0].length;
        final double[][] matATA = new double[numCoefficients][numCoefficients];
        for (int k = 0; k < numCoefficients; ++k) {
            for (int j = 0; j < numCoefficients; ++j) {
                double sum = 0.0;
                for (int i = 0; i < numSamples; ++i) {
                    sum += matA[i][j] * matA[i][k];
                }
                matATA[j][k] = sum;
            }
        }
        final double[][] matInvATA = new double[numCoefficients][numCoefficients];
        if (numCoefficients != 2) {
            throw new RuntimeException(new StringBuilder(78).append("solveLeastSquares: only 2 coefficients currently supported, ").append(numCoefficients).append(" given.").toString());
        }
        final double det = matATA[0][0] * matATA[1][1] - matATA[0][1] * matATA[1][0];
        matInvATA[0][0] = matATA[1][1] / det;
        matInvATA[1][1] = matATA[0][0] / det;
        matInvATA[0][1] = -matATA[1][0] / det;
        matInvATA[1][0] = -matATA[0][1] / det;
        final double[] vecATY = new double[numCoefficients];
        for (int l = 0; l < numCoefficients; ++l) {
            double sum2 = 0.0;
            for (int m = 0; m < numSamples; ++m) {
                sum2 += matA[m][l] * vecY[m];
            }
            vecATY[l] = sum2;
        }
        final double[] vecX = new double[numCoefficients];
        for (int j2 = 0; j2 < numCoefficients; ++j2) {
            double sum3 = 0.0;
            for (int i2 = 0; i2 < numCoefficients; ++i2) {
                sum3 += matInvATA[i2][j2] * vecATY[i2];
            }
            vecX[j2] = sum3;
        }
        return vecX;
    }
    
    public Distortion getApproximateInverseDistortion(final float maxRadius) {
        final int numSamples = 10;
        final int numCoefficients = 2;
        final double[][] matA = new double[10][2];
        final double[] vecY = new double[10];
        for (int i = 0; i < 10; ++i) {
            final float r = maxRadius * (i + 1) / 10.0f;
            double v;
            final double rp = v = this.distort(r);
            for (int j = 0; j < 2; ++j) {
                v *= rp * rp;
                matA[i][j] = v;
            }
            vecY[i] = r - rp;
        }
        final double[] vecK = solveLeastSquares(matA, vecY);
        final float[] coefficients = new float[vecK.length];
        for (int k = 0; k < vecK.length; ++k) {
            coefficients[k] = (float)vecK[k];
        }
        final Distortion inverse = new Distortion();
        inverse.setCoefficients(coefficients);
        return inverse;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Distortion)) {
            return false;
        }
        final Distortion o = (Distortion)other;
        return Arrays.equals(this.mCoefficients, o.mCoefficients);
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append("{\n").append("  coefficients: [");
        for (int i = 0; i < this.mCoefficients.length; ++i) {
            builder.append(Float.toString(this.mCoefficients[i]));
            if (i < this.mCoefficients.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("],\n}");
        return builder.toString();
    }
    
    static {
        DEFAULT_COEFFICIENTS = new float[] { 0.441f, 0.156f };
    }
}
