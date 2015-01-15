package com.google.vrtoolkit.cardboard;

import android.view.*;
import android.util.*;
import com.google.vrtoolkit.cardboard.proto.*;
import java.io.*;

public class ScreenParams
{
    private static final float METERS_PER_INCH = 0.0254f;
    private static final float DEFAULT_BORDER_SIZE_METERS = 0.003f;
    private int mWidth;
    private int mHeight;
    private float mXMetersPerPixel;
    private float mYMetersPerPixel;
    private float mBorderSizeMeters;
    
    public ScreenParams(final Display display) {
        super();
        final DisplayMetrics metrics = new DisplayMetrics();
        try {
            display.getRealMetrics(metrics);
        }
        catch (NoSuchMethodError e) {
            display.getMetrics(metrics);
        }
        this.mXMetersPerPixel = 0.0254f / metrics.xdpi;
        this.mYMetersPerPixel = 0.0254f / metrics.ydpi;
        this.mWidth = metrics.widthPixels;
        this.mHeight = metrics.heightPixels;
        this.mBorderSizeMeters = 0.003f;
        if (this.mHeight > this.mWidth) {
            final int tempPx = this.mWidth;
            this.mWidth = this.mHeight;
            this.mHeight = tempPx;
            final float tempMetersPerPixel = this.mXMetersPerPixel;
            this.mXMetersPerPixel = this.mYMetersPerPixel;
            this.mYMetersPerPixel = tempMetersPerPixel;
        }
    }
    
    public static ScreenParams fromProto(final Display display, final Phone.PhoneParams params) {
        if (params == null) {
            return null;
        }
        final ScreenParams screenParams = new ScreenParams(display);
        if (params.hasXPpi()) {
            screenParams.mXMetersPerPixel = 0.0254f / params.getXPpi();
        }
        if (params.hasYPpi()) {
            screenParams.mYMetersPerPixel = 0.0254f / params.getYPpi();
        }
        if (params.hasBottomBezelHeight()) {
            screenParams.mBorderSizeMeters = params.getBottomBezelHeight();
        }
        return screenParams;
    }
    
    public ScreenParams(final ScreenParams params) {
        super();
        this.mWidth = params.mWidth;
        this.mHeight = params.mHeight;
        this.mXMetersPerPixel = params.mXMetersPerPixel;
        this.mYMetersPerPixel = params.mYMetersPerPixel;
        this.mBorderSizeMeters = params.mBorderSizeMeters;
    }
    
    public void setWidth(final int width) {
        this.mWidth = width;
    }
    
    public int getWidth() {
        return this.mWidth;
    }
    
    public void setHeight(final int height) {
        this.mHeight = height;
    }
    
    public int getHeight() {
        return this.mHeight;
    }
    
    public float getWidthMeters() {
        return this.mWidth * this.mXMetersPerPixel;
    }
    
    public float getHeightMeters() {
        return this.mHeight * this.mYMetersPerPixel;
    }
    
    public void setBorderSizeMeters(final float screenBorderSize) {
        this.mBorderSizeMeters = screenBorderSize;
    }
    
    public float getBorderSizeMeters() {
        return this.mBorderSizeMeters;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof ScreenParams)) {
            return false;
        }
        final ScreenParams o = (ScreenParams)other;
        return this.mWidth == o.mWidth && this.mHeight == o.mHeight && this.mXMetersPerPixel == o.mXMetersPerPixel && this.mYMetersPerPixel == o.mYMetersPerPixel && this.mBorderSizeMeters == o.mBorderSizeMeters;
    }
    
    @Override
    public String toString() {
        return "{\n" + new StringBuilder(22).append("  width: ").append(this.mWidth).append(",\n").toString() + new StringBuilder(23).append("  height: ").append(this.mHeight).append(",\n").toString() + new StringBuilder(39).append("  x_meters_per_pixel: ").append(this.mXMetersPerPixel).append(",\n").toString() + new StringBuilder(39).append("  y_meters_per_pixel: ").append(this.mYMetersPerPixel).append(",\n").toString() + new StringBuilder(39).append("  border_size_meters: ").append(this.mBorderSizeMeters).append(",\n").toString() + "}";
    }
    
    public static ScreenParams createFromInputStream(final Display display, final InputStream inputStream) {
        final Phone.PhoneParams phoneParams = PhoneParams.readFromInputStream(inputStream);
        if (phoneParams == null) {
            return null;
        }
        return fromProto(display, phoneParams);
    }
}
