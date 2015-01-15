package com.google.vrtoolkit.cardboard;

public class Eye
{
    private final int mType;
    private final float[] mEyeView;
    private final Viewport mViewport;
    private final FieldOfView mFov;
    private volatile boolean mProjectionChanged;
    private float[] mPerspective;
    private float mLastZNear;
    private float mLastZFar;
    
    public Eye(final int type) {
        super();
        this.mType = type;
        this.mEyeView = new float[16];
        this.mViewport = new Viewport();
        this.mFov = new FieldOfView();
        this.mProjectionChanged = true;
    }
    
    public int getType() {
        return this.mType;
    }
    
    public float[] getEyeView() {
        return this.mEyeView;
    }
    
    public float[] getPerspective(final float zNear, final float zFar) {
        if (!this.mProjectionChanged && this.mLastZNear == zNear && this.mLastZFar == zFar) {
            return this.mPerspective;
        }
        if (this.mPerspective == null) {
            this.mPerspective = new float[16];
        }
        this.getFov().toPerspectiveMatrix(zNear, zFar, this.mPerspective, 0);
        this.mLastZNear = zNear;
        this.mLastZFar = zFar;
        this.mProjectionChanged = false;
        return this.mPerspective;
    }
    
    public Viewport getViewport() {
        return this.mViewport;
    }
    
    public FieldOfView getFov() {
        return this.mFov;
    }
    
    public void setProjectionChanged() {
        this.mProjectionChanged = true;
    }
    
    public abstract static class Type
    {
        public static final int MONOCULAR = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
    }
}
