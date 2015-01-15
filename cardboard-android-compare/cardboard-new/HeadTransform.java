package com.google.vrtoolkit.cardboard;

import android.opengl.*;
import android.util.*;

public class HeadTransform
{
    private static final float GIMBAL_LOCK_EPSILON = 0.01f;
    private static final float PI = 3.1415927f;
    private final float[] mHeadView;
    
    public HeadTransform() {
        super();
        Matrix.setIdentityM(this.mHeadView = new float[16], 0);
    }
    
    float[] getHeadView() {
        return this.mHeadView;
    }
    
    public void getHeadView(final float[] headView, final int offset) {
        if (offset + 16 > headView.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        System.arraycopy(this.mHeadView, 0, headView, offset, 16);
    }
    
    public void getForwardVector(final float[] forward, final int offset) {
        if (offset + 3 > forward.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i = 0; i < 3; ++i) {
            forward[i + offset] = -this.mHeadView[8 + i];
        }
    }
    
    public void getUpVector(final float[] up, final int offset) {
        if (offset + 3 > up.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i = 0; i < 3; ++i) {
            up[i + offset] = this.mHeadView[4 + i];
        }
    }
    
    public void getRightVector(final float[] right, final int offset) {
        if (offset + 3 > right.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        for (int i = 0; i < 3; ++i) {
            right[i + offset] = this.mHeadView[i];
        }
    }
    
    public void getQuaternion(final float[] quaternion, final int offset) {
        if (offset + 4 > quaternion.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        final float[] m = this.mHeadView;
        final float t = m[0] + m[5] + m[10];
        float w;
        float x;
        float y;
        float z;
        if (t >= 0.0f) {
            float s = FloatMath.sqrt(t + 1.0f);
            w = 0.5f * s;
            s = 0.5f / s;
            x = (m[9] - m[6]) * s;
            y = (m[2] - m[8]) * s;
            z = (m[4] - m[1]) * s;
        }
        else if (m[0] > m[5] && m[0] > m[10]) {
            float s = FloatMath.sqrt(1.0f + m[0] - m[5] - m[10]);
            x = s * 0.5f;
            s = 0.5f / s;
            y = (m[4] + m[1]) * s;
            z = (m[2] + m[8]) * s;
            w = (m[9] - m[6]) * s;
        }
        else if (m[5] > m[10]) {
            float s = FloatMath.sqrt(1.0f + m[5] - m[0] - m[10]);
            y = s * 0.5f;
            s = 0.5f / s;
            x = (m[4] + m[1]) * s;
            z = (m[9] + m[6]) * s;
            w = (m[2] - m[8]) * s;
        }
        else {
            float s = FloatMath.sqrt(1.0f + m[10] - m[0] - m[5]);
            z = s * 0.5f;
            s = 0.5f / s;
            x = (m[2] + m[8]) * s;
            y = (m[9] + m[6]) * s;
            w = (m[4] - m[1]) * s;
        }
        quaternion[offset + 0] = x;
        quaternion[offset + 1] = y;
        quaternion[offset + 2] = z;
        quaternion[offset + 3] = w;
    }
    
    public void getEulerAngles(final float[] eulerAngles, final int offset) {
        if (offset + 3 > eulerAngles.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        final float pitch = (float)Math.asin(this.mHeadView[6]);
        float yaw;
        float roll;
        if (FloatMath.sqrt(1.0f - this.mHeadView[6] * this.mHeadView[6]) >= 0.01f) {
            yaw = (float)Math.atan2(-this.mHeadView[2], this.mHeadView[10]);
            roll = (float)Math.atan2(-this.mHeadView[4], this.mHeadView[5]);
        }
        else {
            yaw = 0.0f;
            roll = (float)Math.atan2(this.mHeadView[1], this.mHeadView[0]);
        }
        eulerAngles[offset + 0] = -pitch;
        eulerAngles[offset + 1] = -yaw;
        eulerAngles[offset + 2] = -roll;
    }
}
