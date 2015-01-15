package com.google.vrtoolkit.cardboard;

import android.content.*;
import android.opengl.Matrix;
import android.view.*;
import android.util.*;
import java.nio.*;
import android.opengl.*;
import android.graphics.*;

class UiLayer
{
    private static final String TAG;
    private static final int NORMAL_COLOR = -3355444;
    private static final int PRESSED_COLOR = -12303292;
    private static final float CENTER_LINE_THICKNESS_DP = 4.0f;
    private static final int BUTTON_WIDTH_DP = 28;
    private static final float TOUCH_SLOP_FACTOR = 1.5f;
    private final int mTouchWidthPx;
    private volatile Rect mTouchRect;
    private boolean mDownWithinBounds;
    private Context mContext;
    private final GLStateBackup mGlStateBackup;
    private final ShaderProgram mShader;
    private final SettingsButtonRenderer mSettingsButtonRenderer;
    private final AlignmentMarkerRenderer mAlignmentMarkerRenderer;
    private Viewport mViewport;
    private boolean mShouldUpdateViewport;
    private boolean mSettingsButtonEnabled;
    private boolean mAlignmentMarkerEnabled;
    private boolean initialized;
    
    UiLayer(final Context context) {
        super();
        this.mTouchRect = new Rect();
        this.mShouldUpdateViewport = true;
        this.mSettingsButtonEnabled = true;
        this.mAlignmentMarkerEnabled = true;
        this.mContext = context;
        final float density = context.getResources().getDisplayMetrics().density;
        final int buttonWidthPx = (int)(28.0f * density);
        this.mTouchWidthPx = (int)(buttonWidthPx * 1.5f);
        this.mGlStateBackup = new GLStateBackup();
        this.mShader = new ShaderProgram();
        this.mSettingsButtonRenderer = new SettingsButtonRenderer(this.mShader, buttonWidthPx);
        this.mAlignmentMarkerRenderer = new AlignmentMarkerRenderer(this.mShader, this.mTouchWidthPx, 4.0f * density);
        this.mViewport = new Viewport();
    }
    
    void updateViewport(final Viewport viewport) {
        synchronized (this) {
            if (this.mViewport.equals(viewport)) {
                return;
            }
            final int w = viewport.width;
            final int h = viewport.height;
            this.mTouchRect = new Rect((w - this.mTouchWidthPx) / 2, h - this.mTouchWidthPx, (w + this.mTouchWidthPx) / 2, h);
            this.mViewport.setViewport(viewport.x, viewport.y, viewport.width, viewport.height);
            this.mShouldUpdateViewport = true;
        }
    }
    
    void initializeGl() {
        this.mShader.initializeGl();
        this.mGlStateBackup.clearTrackedVertexAttributes();
        this.mGlStateBackup.addTrackedVertexAttribute(this.mShader.aPosition);
        this.mGlStateBackup.readFromGL();
        this.mSettingsButtonRenderer.initializeGl();
        this.mAlignmentMarkerRenderer.initializeGl();
        this.mGlStateBackup.writeToGL();
        this.initialized = true;
    }
    
    void draw() {
        if (!this.getSettingsButtonEnabled() && !this.getAlignmentMarkerEnabled()) {
            return;
        }
        if (!this.initialized) {
            this.initializeGl();
        }
        this.mGlStateBackup.readFromGL();
        synchronized (this) {
            if (this.mShouldUpdateViewport) {
                this.mShouldUpdateViewport = false;
                this.mSettingsButtonRenderer.updateViewport(this.mViewport);
                this.mAlignmentMarkerRenderer.updateViewport(this.mViewport);
            }
            this.mViewport.setGLViewport();
        }
        if (this.getSettingsButtonEnabled()) {
            this.mSettingsButtonRenderer.draw();
        }
        if (this.getAlignmentMarkerEnabled()) {
            this.mAlignmentMarkerRenderer.draw();
        }
        this.mGlStateBackup.writeToGL();
    }
    
    synchronized void setAlignmentMarkerEnabled(final boolean enabled) {
        if (this.mAlignmentMarkerEnabled != enabled) {
            this.mAlignmentMarkerEnabled = enabled;
            this.mShouldUpdateViewport = true;
        }
    }
    
    synchronized boolean getAlignmentMarkerEnabled() {
        return this.mAlignmentMarkerEnabled;
    }
    
    synchronized void setSettingsButtonEnabled(final boolean enabled) {
        if (this.mSettingsButtonEnabled != enabled) {
            this.mSettingsButtonEnabled = enabled;
            this.mShouldUpdateViewport = true;
        }
    }
    
    synchronized boolean getSettingsButtonEnabled() {
        return this.mSettingsButtonEnabled;
    }
    
    boolean onTouchEvent(final MotionEvent e) {
        boolean touchWithinBounds = false;
        synchronized (this) {
            if (!this.mSettingsButtonEnabled) {
                return false;
            }
            touchWithinBounds = this.mTouchRect.contains((int)e.getX(), (int)e.getY());
        }
        if (e.getActionMasked() == 0 && touchWithinBounds) {
            this.mDownWithinBounds = true;
        }
        if (!this.mDownWithinBounds) {
            return false;
        }
        if (e.getActionMasked() == 1) {
            if (touchWithinBounds) {
                UiUtils.launchOrInstallCardboard(this.mContext);
            }
            this.mDownWithinBounds = false;
        }
        else if (e.getActionMasked() == 3) {
            this.mDownWithinBounds = false;
        }
        this.setPressed(this.mDownWithinBounds && touchWithinBounds);
        return true;
    }
    
    private void setPressed(final boolean pressed) {
        if (this.mSettingsButtonRenderer != null) {
            this.mSettingsButtonRenderer.setColor(pressed ? -12303292 : -3355444);
        }
    }
    
    private static void checkGlError(final String op) {
        final int error;
        if ((error = GLES20.glGetError()) != 0) {
            final String tag = UiLayer.TAG;
            final String value = String.valueOf(String.valueOf(op));
            Log.e(tag, new StringBuilder(21 + value.length()).append(value).append(": glError ").append(error).toString());
            final String value2 = String.valueOf(String.valueOf(op));
            throw new RuntimeException(new StringBuilder(21 + value2.length()).append(value2).append(": glError ").append(error).toString());
        }
    }
    
    private static float lerp(final float a, final float b, final float t) {
        return a * (1.0f - t) + b * t;
    }
    
    static {
        TAG = UiLayer.class.getSimpleName();
    }
    
    private static class ShaderProgram
    {
        private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nattribute vec2 aPosition;\nvoid main() {\n    gl_Position = uMVPMatrix * vec4(aPosition, 0.0, 1.0);\n}\n";
        private static final String FRAGMENT_SHADER = "precision mediump float;\nuniform vec4 uColor;\nvoid main() {\n    gl_FragColor = uColor;\n}\n";
        public int program;
        public int aPosition;
        public int uMvpMatrix;
        public int uColor;
        
        void initializeGl() {
            this.program = this.createProgram("uniform mat4 uMVPMatrix;\nattribute vec2 aPosition;\nvoid main() {\n    gl_Position = uMVPMatrix * vec4(aPosition, 0.0, 1.0);\n}\n", "precision mediump float;\nuniform vec4 uColor;\nvoid main() {\n    gl_FragColor = uColor;\n}\n");
            if (this.program == 0) {
                throw new RuntimeException("Could not create program");
            }
            this.aPosition = GLES20.glGetAttribLocation(this.program, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (this.aPosition == -1) {
                throw new RuntimeException("Could not get attrib location for aPosition");
            }
            this.uMvpMatrix = GLES20.glGetUniformLocation(this.program, "uMVPMatrix");
            if (this.uMvpMatrix == -1) {
                throw new RuntimeException("Could not get uniform location for uMVPMatrix");
            }
            this.uColor = GLES20.glGetUniformLocation(this.program, "uColor");
            if (this.uColor == -1) {
                throw new RuntimeException("Could not get uniform location for uColor");
            }
        }
        
        private int loadShader(final int shaderType, final String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader != 0) {
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                final int[] compiled = { 0 };
                GLES20.glGetShaderiv(shader, 35713, compiled, 0);
                if (compiled[0] == 0) {
                    Log.e(UiLayer.TAG, new StringBuilder(37).append("Could not compile shader ").append(shaderType).append(":").toString());
                    Log.e(UiLayer.TAG, GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                }
            }
            return shader;
        }
        
        private int createProgram(final String vertexSource, final String fragmentSource) {
            final int vertexShader = this.loadShader(35633, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            final int pixelShader = this.loadShader(35632, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }
            int program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(program, pixelShader);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(program);
                final int[] linkStatus = { 0 };
                GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
                if (linkStatus[0] != 1) {
                    Log.e(UiLayer.TAG, "Could not link program: ");
                    Log.e(UiLayer.TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
                checkGlError("glLinkProgram");
            }
            return program;
        }
    }
    
    private static class MeshRenderer
    {
        private static final int BYTES_PER_FLOAT = 4;
        private static final int BYTES_PER_SHORT = 4;
        protected static final int COMPONENTS_PER_VERT = 2;
        private static final int DATA_STRIDE_BYTES = 8;
        private static final int DATA_POS_OFFSET = 0;
        protected int mArrayBufferId;
        protected int mElementBufferId;
        protected ShaderProgram mShader;
        protected float[] mMvp;
        private int mNumIndices;
        
        MeshRenderer(final ShaderProgram shader) {
            super();
            this.mArrayBufferId = -1;
            this.mElementBufferId = -1;
            this.mMvp = new float[16];
            this.mShader = shader;
        }
        
        void genAndBindBuffers(final float[] vertexData, final short[] indexData) {
            final FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(vertexData).position(0);
            this.mNumIndices = indexData.length;
            final ShortBuffer indexBuffer = ByteBuffer.allocateDirect(this.mNumIndices * 4).order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBuffer.put(indexData).position(0);
            final int[] bufferIds = new int[2];
            GLES20.glGenBuffers(2, bufferIds, 0);
            this.mArrayBufferId = bufferIds[0];
            this.mElementBufferId = bufferIds[1];
            GLES20.glBindBuffer(34962, this.mArrayBufferId);
            GLES20.glBufferData(34962, vertexData.length * 4, (Buffer)vertexBuffer, 35044);
            GLES20.glBindBuffer(34963, this.mElementBufferId);
            GLES20.glBufferData(34963, indexData.length * 4, (Buffer)indexBuffer, 35044);
            checkGlError("genAndBindBuffers");
        }
        
        void updateViewport(final Viewport viewport) {
            Matrix.setIdentityM(this.mMvp, 0);
        }
        
        void draw() {
            GLES20.glDisable(2929);
            GLES20.glDisable(2884);
            GLES20.glUseProgram(this.mShader.program);
            GLES20.glUniformMatrix4fv(this.mShader.uMvpMatrix, 1, false, this.mMvp, 0);
            GLES20.glBindBuffer(34962, this.mArrayBufferId);
            GLES20.glVertexAttribPointer(this.mShader.aPosition, 2, 5126, false, 8, 0);
            GLES20.glEnableVertexAttribArray(this.mShader.aPosition);
            GLES20.glBindBuffer(34963, this.mElementBufferId);
            GLES20.glDrawElements(5, this.mNumIndices, 5123, 0);
        }
    }
    
    private static class AlignmentMarkerRenderer extends MeshRenderer
    {
        private static final int COLOR;
        private float mVerticalBorderPaddingPx;
        private float mLineThicknessPx;
        
        AlignmentMarkerRenderer(final ShaderProgram shader, final float verticalBorderPaddingPx, final float lineThicknessPx) {
            super(shader);
            this.mVerticalBorderPaddingPx = verticalBorderPaddingPx;
            this.mLineThicknessPx = lineThicknessPx;
        }
        
        void initializeGl() {
            final float[] vertexData = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
            final short[] indexData = new short[vertexData.length / 2];
            for (int i = 0; i < indexData.length; ++i) {
                indexData[i] = (short)i;
            }
            this.genAndBindBuffers(vertexData, indexData);
        }
        
        @Override
        void updateViewport(final Viewport viewport) {
            Matrix.setIdentityM(this.mMvp, 0);
            final float xScale = this.mLineThicknessPx / viewport.width;
            final float yScale = 1.0f - 2.0f * this.mVerticalBorderPaddingPx / viewport.height;
            Matrix.scaleM(this.mMvp, 0, xScale, yScale, 1.0f);
        }
        
        @Override
        void draw() {
            GLES20.glUseProgram(this.mShader.program);
            GLES20.glUniform4f(this.mShader.uColor, Color.red(AlignmentMarkerRenderer.COLOR) / 255.0f, Color.green(AlignmentMarkerRenderer.COLOR) / 255.0f, Color.blue(AlignmentMarkerRenderer.COLOR) / 255.0f, Color.alpha(AlignmentMarkerRenderer.COLOR) / 255.0f);
            super.draw();
        }
        
        static {
            COLOR = Color.argb(255, 50, 50, 50);
        }
    }
    
    private static class SettingsButtonRenderer extends MeshRenderer
    {
        private static final int DEGREES_PER_GEAR_SECTION = 60;
        private static final int OUTER_RIM_END_DEG = 12;
        private static final int INNER_RIM_BEGIN_DEG = 20;
        private static final float OUTER_RADIUS = 1.0f;
        private static final float MIDDLE_RADIUS = 0.75f;
        private static final float INNER_RADIUS = 0.3125f;
        private static final int NUM_VERTICES = 60;
        private int mButtonWidthPx;
        private int mColor;
        
        SettingsButtonRenderer(final ShaderProgram shader, final int buttonWidthPx) {
            super(shader);
            this.mColor = -3355444;
            this.mButtonWidthPx = buttonWidthPx;
        }
        
        void initializeGl() {
            final float[] vertexData = new float[120];
            final int numVerticesPerRim = 30;
            final float lerpInterval = 8.0f;
            for (int i = 0; i < numVerticesPerRim; ++i) {
                final float theta = i / numVerticesPerRim * 360.0f;
                final float mod = theta % 60.0f;
                float r;
                if (mod <= 12.0f) {
                    r = 1.0f;
                }
                else if (mod <= 20.0f) {
                    r = lerp(1.0f, 0.75f, (mod - 12.0f) / lerpInterval);
                }
                else if (mod <= 40.0f) {
                    r = 0.75f;
                }
                else if (mod <= 48.0f) {
                    r = lerp(0.75f, 1.0f, (mod - 60.0f + 20.0f) / lerpInterval);
                }
                else {
                    r = 1.0f;
                }
                vertexData[2 * i] = r * (float)Math.cos(Math.toRadians(90.0f - theta));
                vertexData[2 * i + 1] = r * (float)Math.sin(Math.toRadians(90.0f - theta));
            }
            final int innerStartingIndex = 2 * numVerticesPerRim;
            for (int j = 0; j < numVerticesPerRim; ++j) {
                final float theta2 = j / numVerticesPerRim * 360.0f;
                vertexData[innerStartingIndex + 2 * j] = 0.3125f * (float)Math.cos(Math.toRadians(90.0f - theta2));
                vertexData[innerStartingIndex + 2 * j + 1] = 0.3125f * (float)Math.sin(Math.toRadians(90.0f - theta2));
            }
            final short[] indexData = new short[62];
            for (int k = 0; k < numVerticesPerRim; ++k) {
                indexData[2 * k] = (short)k;
                indexData[2 * k + 1] = (short)(numVerticesPerRim + k);
            }
            indexData[indexData.length - 2] = 0;
            indexData[indexData.length - 1] = (short)numVerticesPerRim;
            this.genAndBindBuffers(vertexData, indexData);
        }
        
        synchronized void setColor(final int color) {
            this.mColor = color;
        }
        
        @Override
        void updateViewport(final Viewport viewport) {
            Matrix.setIdentityM(this.mMvp, 0);
            final float yScale = this.mButtonWidthPx / viewport.height;
            final float xScale = yScale * viewport.height / viewport.width;
            Matrix.translateM(this.mMvp, 0, 0.0f, yScale - 1.0f, 0.0f);
            Matrix.scaleM(this.mMvp, 0, xScale, yScale, 1.0f);
        }
        
        @Override
        void draw() {
            GLES20.glUseProgram(this.mShader.program);
            synchronized (this) {
                GLES20.glUniform4f(this.mShader.uColor, Color.red(this.mColor) / 255.0f, Color.green(this.mColor) / 255.0f, Color.blue(this.mColor) / 255.0f, Color.alpha(this.mColor) / 255.0f);
            }
            super.draw();
        }
    }
}
