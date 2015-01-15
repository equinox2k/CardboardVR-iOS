package com.google.vrtoolkit.cardboard;

import android.opengl.*;

public class Viewport
{
    public int x;
    public int y;
    public int width;
    public int height;
    
    public void setViewport(final int x, final int y, final int width, final int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void setGLViewport() {
        GLES20.glViewport(this.x, this.y, this.width, this.height);
    }
    
    public void setGLScissor() {
        GLES20.glScissor(this.x, this.y, this.width, this.height);
    }
    
    public void getAsArray(final int[] array, final int offset) {
        if (offset + 4 > array.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        array[offset] = this.x;
        array[offset + 1] = this.y;
        array[offset + 2] = this.width;
        array[offset + 3] = this.height;
    }
    
    @Override
    public String toString() {
        return "{\n" + new StringBuilder(18).append("  x: ").append(this.x).append(",\n").toString() + new StringBuilder(18).append("  y: ").append(this.y).append(",\n").toString() + new StringBuilder(22).append("  width: ").append(this.width).append(",\n").toString() + new StringBuilder(23).append("  height: ").append(this.height).append(",\n").toString() + "}";
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Viewport)) {
            return false;
        }
        final Viewport other = (Viewport)obj;
        return this.x == other.x && this.y == other.y && this.width == other.width && this.height == other.height;
    }
    
    @Override
    public int hashCode() {
        return Integer.valueOf(this.x).hashCode() ^ Integer.valueOf(this.y).hashCode() ^ Integer.valueOf(this.width).hashCode() ^ Integer.valueOf(this.height).hashCode();
    }
}
