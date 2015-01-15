package com.google.vrtoolkit.cardboard.sensors.internal;

public class Matrix3x3d
{
    public double[] m;
    
    public Matrix3x3d() {
        super();
        this.m = new double[9];
    }
    
    public Matrix3x3d(final double m00, final double m01, final double m02, final double m10, final double m11, final double m12, final double m20, final double m21, final double m22) {
        super();
        (this.m = new double[9])[0] = m00;
        this.m[1] = m01;
        this.m[2] = m02;
        this.m[3] = m10;
        this.m[4] = m11;
        this.m[5] = m12;
        this.m[6] = m20;
        this.m[7] = m21;
        this.m[8] = m22;
    }
    
    public Matrix3x3d(final Matrix3x3d o) {
        super();
        (this.m = new double[9])[0] = o.m[0];
        this.m[1] = o.m[1];
        this.m[2] = o.m[2];
        this.m[3] = o.m[3];
        this.m[4] = o.m[4];
        this.m[5] = o.m[5];
        this.m[6] = o.m[6];
        this.m[7] = o.m[7];
        this.m[8] = o.m[8];
    }
    
    public void set(final double m00, final double m01, final double m02, final double m10, final double m11, final double m12, final double m20, final double m21, final double m22) {
        this.m[0] = m00;
        this.m[1] = m01;
        this.m[2] = m02;
        this.m[3] = m10;
        this.m[4] = m11;
        this.m[5] = m12;
        this.m[6] = m20;
        this.m[7] = m21;
        this.m[8] = m22;
    }
    
    public void set(final Matrix3x3d o) {
        this.m[0] = o.m[0];
        this.m[1] = o.m[1];
        this.m[2] = o.m[2];
        this.m[3] = o.m[3];
        this.m[4] = o.m[4];
        this.m[5] = o.m[5];
        this.m[6] = o.m[6];
        this.m[7] = o.m[7];
        this.m[8] = o.m[8];
    }
    
    public void setZero() {
        this.m[0] = 0;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;
        this.m[4] = 0;
        this.m[5] = 0;
        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 0;
    }
    
    public void setIdentity() {
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;
        this.m[4] = 1;
        this.m[5] = 0;
        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;
    }
    
    public void setSameDiagonal(final double d) {
        final double[] m = this.m;
        final int n = 0;
        final double[] i = this.m;
        final int n2 = 4;
        this.m[8] = d;
        m[n] = (i[n2] = d);
    }
    
    public double get(final int row, final int col) {
        return this.m[3 * row + col];
    }
    
    public void set(final int row, final int col, final double value) {
        this.m[3 * row + col] = value;
    }
    
    public void getColumn(final int col, final Vector3d v) {
        v.x = this.m[col];
        v.y = this.m[col + 3];
        v.z = this.m[col + 6];
    }
    
    public void setColumn(final int col, final Vector3d v) {
        this.m[col] = v.x;
        this.m[col + 3] = v.y;
        this.m[col + 6] = v.z;
    }
    
    public void scale(final double s) {
        final double[] m = this.m;
        final int n = 0;
        m[n] *= s;
        final double[] i = this.m;
        final int n2 = 1;
        i[n2] *= s;
        final double[] j = this.m;
        final int n3 = 2;
        j[n3] *= s;
        final double[] k = this.m;
        final int n4 = 3;
        k[n4] *= s;
        final double[] l = this.m;
        final int n5 = 4;
        l[n5] *= s;
        final double[] m2 = this.m;
        final int n6 = 5;
        m2[n6] *= s;
        final double[] m3 = this.m;
        final int n7 = 6;
        m3[n7] *= s;
        final double[] m4 = this.m;
        final int n8 = 7;
        m4[n8] *= s;
        final double[] m5 = this.m;
        final int n9 = 8;
        m5[n9] *= s;
    }
    
    public void plusEquals(final Matrix3x3d b) {
        final double[] m = this.m;
        final int n = 0;
        m[n] += b.m[0];
        final double[] i = this.m;
        final int n2 = 1;
        i[n2] += b.m[1];
        final double[] j = this.m;
        final int n3 = 2;
        j[n3] += b.m[2];
        final double[] k = this.m;
        final int n4 = 3;
        k[n4] += b.m[3];
        final double[] l = this.m;
        final int n5 = 4;
        l[n5] += b.m[4];
        final double[] m2 = this.m;
        final int n6 = 5;
        m2[n6] += b.m[5];
        final double[] m3 = this.m;
        final int n7 = 6;
        m3[n7] += b.m[6];
        final double[] m4 = this.m;
        final int n8 = 7;
        m4[n8] += b.m[7];
        final double[] m5 = this.m;
        final int n9 = 8;
        m5[n9] += b.m[8];
    }
    
    public void minusEquals(final Matrix3x3d b) {
        final double[] m = this.m;
        final int n = 0;
        m[n] -= b.m[0];
        final double[] i = this.m;
        final int n2 = 1;
        i[n2] -= b.m[1];
        final double[] j = this.m;
        final int n3 = 2;
        j[n3] -= b.m[2];
        final double[] k = this.m;
        final int n4 = 3;
        k[n4] -= b.m[3];
        final double[] l = this.m;
        final int n5 = 4;
        l[n5] -= b.m[4];
        final double[] m2 = this.m;
        final int n6 = 5;
        m2[n6] -= b.m[5];
        final double[] m3 = this.m;
        final int n7 = 6;
        m3[n7] -= b.m[6];
        final double[] m4 = this.m;
        final int n8 = 7;
        m4[n8] -= b.m[7];
        final double[] m5 = this.m;
        final int n9 = 8;
        m5[n9] -= b.m[8];
    }
    
    public void transpose() {
        double tmp = this.m[1];
        this.m[1] = this.m[3];
        this.m[3] = tmp;
        tmp = this.m[2];
        this.m[2] = this.m[6];
        this.m[6] = tmp;
        tmp = this.m[5];
        this.m[5] = this.m[7];
        this.m[7] = tmp;
    }
    
    public void transpose(final Matrix3x3d result) {
        final double m1 = this.m[1];
        final double m2 = this.m[2];
        final double m3 = this.m[5];
        result.m[0] = this.m[0];
        result.m[1] = this.m[3];
        result.m[2] = this.m[6];
        result.m[3] = m1;
        result.m[4] = this.m[4];
        result.m[5] = this.m[7];
        result.m[6] = m2;
        result.m[7] = m3;
        result.m[8] = this.m[8];
    }
    
    public static void add(final Matrix3x3d a, final Matrix3x3d b, final Matrix3x3d result) {
        result.m[0] = a.m[0] + b.m[0];
        result.m[1] = a.m[1] + b.m[1];
        result.m[2] = a.m[2] + b.m[2];
        result.m[3] = a.m[3] + b.m[3];
        result.m[4] = a.m[4] + b.m[4];
        result.m[5] = a.m[5] + b.m[5];
        result.m[6] = a.m[6] + b.m[6];
        result.m[7] = a.m[7] + b.m[7];
        result.m[8] = a.m[8] + b.m[8];
    }
    
    public static void mult(final Matrix3x3d a, final Matrix3x3d b, final Matrix3x3d result) {
        result.set(a.m[0] * b.m[0] + a.m[1] * b.m[3] + a.m[2] * b.m[6], a.m[0] * b.m[1] + a.m[1] * b.m[4] + a.m[2] * b.m[7], a.m[0] * b.m[2] + a.m[1] * b.m[5] + a.m[2] * b.m[8], a.m[3] * b.m[0] + a.m[4] * b.m[3] + a.m[5] * b.m[6], a.m[3] * b.m[1] + a.m[4] * b.m[4] + a.m[5] * b.m[7], a.m[3] * b.m[2] + a.m[4] * b.m[5] + a.m[5] * b.m[8], a.m[6] * b.m[0] + a.m[7] * b.m[3] + a.m[8] * b.m[6], a.m[6] * b.m[1] + a.m[7] * b.m[4] + a.m[8] * b.m[7], a.m[6] * b.m[2] + a.m[7] * b.m[5] + a.m[8] * b.m[8]);
    }
    
    public static void mult(final Matrix3x3d a, final Vector3d v, final Vector3d result) {
        final double x = a.m[0] * v.x + a.m[1] * v.y + a.m[2] * v.z;
        final double y = a.m[3] * v.x + a.m[4] * v.y + a.m[5] * v.z;
        final double z = a.m[6] * v.x + a.m[7] * v.y + a.m[8] * v.z;
        result.x = x;
        result.y = y;
        result.z = z;
    }
    
    public double determinant() {
        return this.get(0, 0) * (this.get(1, 1) * this.get(2, 2) - this.get(2, 1) * this.get(1, 2)) - this.get(0, 1) * (this.get(1, 0) * this.get(2, 2) - this.get(1, 2) * this.get(2, 0)) + this.get(0, 2) * (this.get(1, 0) * this.get(2, 1) - this.get(1, 1) * this.get(2, 0));
    }
    
    public boolean invert(final Matrix3x3d result) {
        final double d = this.determinant();
        if (d == 0.0) {
            return false;
        }
        final double invdet = 1.0 / d;
        result.set((this.m[4] * this.m[8] - this.m[7] * this.m[5]) * invdet, -(this.m[1] * this.m[8] - this.m[2] * this.m[7]) * invdet, (this.m[1] * this.m[5] - this.m[2] * this.m[4]) * invdet, -(this.m[3] * this.m[8] - this.m[5] * this.m[6]) * invdet, (this.m[0] * this.m[8] - this.m[2] * this.m[6]) * invdet, -(this.m[0] * this.m[5] - this.m[3] * this.m[2]) * invdet, (this.m[3] * this.m[7] - this.m[6] * this.m[4]) * invdet, -(this.m[0] * this.m[7] - this.m[6] * this.m[1]) * invdet, (this.m[0] * this.m[4] - this.m[3] * this.m[1]) * invdet);
        return true;
    }
}
