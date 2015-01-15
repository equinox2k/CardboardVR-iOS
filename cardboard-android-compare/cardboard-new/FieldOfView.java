package com.google.vrtoolkit.cardboard;

import android.opengl.*;

public class FieldOfView
{
    private static final float DEFAULT_MAX_FOV_LEFT_RIGHT = 40.0f;
    private static final float DEFAULT_MAX_FOV_BOTTOM = 40.0f;
    private static final float DEFAULT_MAX_FOV_TOP = 40.0f;
    private float mLeft;
    private float mRight;
    private float mBottom;
    private float mTop;
    
    public FieldOfView() {
        super();
        this.mLeft = 40.0f;
        this.mRight = 40.0f;
        this.mBottom = 40.0f;
        this.mTop = 40.0f;
    }
    
    public FieldOfView(final float left, final float right, final float bottom, final float top) {
        super();
        this.mLeft = left;
        this.mRight = right;
        this.mBottom = bottom;
        this.mTop = top;
    }
    
    public FieldOfView(final FieldOfView other) {
        super();
        this.copy(other);
    }
    
    public static FieldOfView parseFromProtobuf(final float[] angles) {
        if (angles.length != 4) {
            return null;
        }
        return new FieldOfView(angles[0], angles[1], angles[2], angles[3]);
    }
    
    public float[] toProtobuf() {
        return new float[] { this.mLeft, this.mRight, this.mBottom, this.mTop };
    }
    
    public void copy(final FieldOfView other) {
        this.mLeft = other.mLeft;
        this.mRight = other.mRight;
        this.mBottom = other.mBottom;
        this.mTop = other.mTop;
    }
    
    public void setLeft(final float left) {
        this.mLeft = left;
    }
    
    public float getLeft() {
        return this.mLeft;
    }
    
    public void setRight(final float right) {
        this.mRight = right;
    }
    
    public float getRight() {
        return this.mRight;
    }
    
    public void setBottom(final float bottom) {
        this.mBottom = bottom;
    }
    
    public float getBottom() {
        return this.mBottom;
    }
    
    public void setTop(final float top) {
        this.mTop = top;
    }
    
    public float getTop() {
        return this.mTop;
    }
    
    public void toPerspectiveMatrix(final float near, final float far, final float[] perspective, final int offset) {
        if (offset + 16 > perspective.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        final float l = (float)(-Math.tan(Math.toRadians(this.mLeft))) * near;
        final float r = (float)Math.tan(Math.toRadians(this.mRight)) * near;
        final float b = (float)(-Math.tan(Math.toRadians(this.mBottom))) * near;
        final float t = (float)Math.tan(Math.toRadians(this.mTop)) * near;
        Matrix.frustumM(perspective, offset, l, r, b, t, near, far);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof FieldOfView)) {
            return false;
        }
        final FieldOfView o = (FieldOfView)other;
        return this.mLeft == o.mLeft && this.mRight == o.mRight && this.mBottom == o.mBottom && this.mTop == o.mTop;
    }
    
    @Override
    public String toString() {
        return "{\n" + new StringBuilder(25).append("  left: ").append(this.mLeft).append(",\n").toString() + new StringBuilder(26).append("  right: ").append(this.mRight).append(",\n").toString() + new StringBuilder(27).append("  bottom: ").append(this.mBottom).append(",\n").toString() + new StringBuilder(24).append("  top: ").append(this.mTop).append(",\n").toString() + "}";
    }
}
