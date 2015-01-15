package com.google.vrtoolkit.cardboard;

import android.opengl.*;
import android.util.*;
import java.nio.*;

public class DistortionRenderer
{
    private static final String TAG = "DistortionRenderer";
    private int mTextureId;
    private int mRenderbufferId;
    private int mFramebufferId;
    private IntBuffer mOriginalFramebufferId;
    private int mTextureFormat;
    private int mTextureType;
    private float mResolutionScale;
    private boolean mRestoreGLStateEnabled;
    private boolean mChromaticAberrationCorrectionEnabled;
    private boolean mVignetteEnabled;
    private DistortionMesh mLeftEyeDistortionMesh;
    private DistortionMesh mRightEyeDistortionMesh;
    private GLStateBackup mGLStateBackup;
    private GLStateBackup mGLStateBackupAberration;
    private HeadMountedDisplay mHmd;
    private EyeViewport mLeftEyeViewport;
    private EyeViewport mRightEyeViewport;
    private boolean mFovsChanged;
    private boolean mViewportsChanged;
    private boolean mTextureFormatChanged;
    private boolean mDrawingFrame;
    private float mXPxPerTanAngle;
    private float mYPxPerTanAngle;
    private float mMetersPerTanAngle;
    private ProgramHolder mProgramHolder;
    private ProgramHolderAberration mProgramHolderAberration;
    static final String VERTEX_SHADER = "attribute vec2 aPosition;\nattribute float aVignette;\nattribute vec2 aBlueTextureCoord;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform float uTextureCoordScale;\nvoid main() {\n    gl_Position = vec4(aPosition, 0.0, 1.0);\n    vTextureCoord = aBlueTextureCoord.xy * uTextureCoordScale;\n    vVignette = aVignette;\n}\n";
    static final String FRAGMENT_SHADER = "precision mediump float;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform sampler2D uTextureSampler;\nvoid main() {\n    gl_FragColor = vVignette * texture2D(uTextureSampler, vTextureCoord);\n}\n";
    static final String VERTEX_SHADER_ABERRATION = "attribute vec2 aPosition;\nattribute float aVignette;\nattribute vec2 aRedTextureCoord;\nattribute vec2 aGreenTextureCoord;\nattribute vec2 aBlueTextureCoord;\nvarying vec2 vRedTextureCoord;\nvarying vec2 vBlueTextureCoord;\nvarying vec2 vGreenTextureCoord;\nvarying float vVignette;\nuniform float uTextureCoordScale;\nvoid main() {\n    gl_Position = vec4(aPosition, 0.0, 1.0);\n    vRedTextureCoord = aRedTextureCoord.xy * uTextureCoordScale;\n    vGreenTextureCoord = aGreenTextureCoord.xy * uTextureCoordScale;\n    vBlueTextureCoord = aBlueTextureCoord.xy * uTextureCoordScale;\n    vVignette = aVignette;\n}\n";
    static final String FRAGMENT_SHADER_ABERRATION = "precision mediump float;\nvarying vec2 vRedTextureCoord;\nvarying vec2 vBlueTextureCoord;\nvarying vec2 vGreenTextureCoord;\nvarying float vVignette;\nuniform sampler2D uTextureSampler;\nvoid main() {\n    gl_FragColor = vVignette * vec4(texture2D(uTextureSampler, vRedTextureCoord).r,\n                    texture2D(uTextureSampler, vGreenTextureCoord).g,\n                    texture2D(uTextureSampler, vBlueTextureCoord).b, 1.0);\n}\n";
    
    public DistortionRenderer() {
        super();
        this.mTextureId = -1;
        this.mRenderbufferId = -1;
        this.mFramebufferId = -1;
        this.mOriginalFramebufferId = IntBuffer.allocate(1);
        this.mTextureFormat = 6407;
        this.mTextureType = 5121;
        this.mResolutionScale = 1.0f;
        this.mGLStateBackup = new GLStateBackup();
        this.mGLStateBackupAberration = new GLStateBackup();
    }
    
    public void setTextureFormat(final int textureFormat, final int textureType) {
        if (this.mDrawingFrame) {
            throw new IllegalStateException("Cannot change texture format during rendering.");
        }
        if (textureFormat != this.mTextureFormat || textureType != this.mTextureType) {
            this.mTextureFormat = textureFormat;
            this.mTextureType = textureType;
            this.mTextureFormatChanged = true;
        }
    }
    
    public void beforeDrawFrame() {
        this.mDrawingFrame = true;
        if (this.mFovsChanged || this.mTextureFormatChanged) {
            this.updateTextureAndDistortionMesh();
        }
        GLES20.glGetIntegerv(36006, this.mOriginalFramebufferId);
        GLES20.glBindFramebuffer(36160, this.mFramebufferId);
    }
    
    public void afterDrawFrame() {
        GLES20.glBindFramebuffer(36160, this.mOriginalFramebufferId.array()[0]);
        this.undistortTexture(this.mTextureId);
        this.mDrawingFrame = false;
    }
    
    public void undistortTexture(final int textureId) {
        if (this.mRestoreGLStateEnabled) {
            if (this.mChromaticAberrationCorrectionEnabled) {
                this.mGLStateBackupAberration.readFromGL();
            }
            else {
                this.mGLStateBackup.readFromGL();
            }
        }
        if (this.mFovsChanged || this.mTextureFormatChanged) {
            this.updateTextureAndDistortionMesh();
        }
        GLES20.glViewport(0, 0, this.mHmd.getScreenParams().getWidth(), this.mHmd.getScreenParams().getHeight());
        GLES20.glDisable(3089);
        GLES20.glDisable(2884);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(16640);
        if (this.mChromaticAberrationCorrectionEnabled) {
            GLES20.glUseProgram(this.mProgramHolderAberration.program);
        }
        else {
            GLES20.glUseProgram(this.mProgramHolder.program);
        }
        GLES20.glEnable(3089);
        GLES20.glScissor(0, 0, this.mHmd.getScreenParams().getWidth() / 2, this.mHmd.getScreenParams().getHeight());
        this.renderDistortionMesh(this.mLeftEyeDistortionMesh, textureId);
        GLES20.glScissor(this.mHmd.getScreenParams().getWidth() / 2, 0, this.mHmd.getScreenParams().getWidth() / 2, this.mHmd.getScreenParams().getHeight());
        this.renderDistortionMesh(this.mRightEyeDistortionMesh, textureId);
        if (this.mRestoreGLStateEnabled) {
            if (this.mChromaticAberrationCorrectionEnabled) {
                this.mGLStateBackupAberration.writeToGL();
            }
            else {
                this.mGLStateBackup.writeToGL();
            }
        }
    }
    
    public void setResolutionScale(final float scale) {
        this.mResolutionScale = scale;
        this.mViewportsChanged = true;
    }
    
    public void setRestoreGLStateEnabled(final boolean enabled) {
        this.mRestoreGLStateEnabled = enabled;
    }
    
    public void setChromaticAberrationCorrectionEnabled(final boolean enabled) {
        this.mChromaticAberrationCorrectionEnabled = enabled;
    }
    
    public void setVignetteEnabled(final boolean enabled) {
        this.mVignetteEnabled = enabled;
        this.mFovsChanged = true;
    }
    
    public void onFovChanged(final HeadMountedDisplay hmd, final FieldOfView leftFov, final FieldOfView rightFov, final float virtualEyeToScreenDistance) {
        if (this.mDrawingFrame) {
            throw new IllegalStateException("Cannot change FOV while rendering a frame.");
        }
        this.mHmd = new HeadMountedDisplay(hmd);
        this.mLeftEyeViewport = this.initViewportForEye(leftFov, 0.0f);
        this.mRightEyeViewport = this.initViewportForEye(rightFov, this.mLeftEyeViewport.width);
        this.mMetersPerTanAngle = virtualEyeToScreenDistance;
        final ScreenParams screen = this.mHmd.getScreenParams();
        this.mXPxPerTanAngle = screen.getWidth() / (screen.getWidthMeters() / this.mMetersPerTanAngle);
        this.mYPxPerTanAngle = screen.getHeight() / (screen.getHeightMeters() / this.mMetersPerTanAngle);
        this.mFovsChanged = true;
        this.mViewportsChanged = true;
    }
    
    public boolean haveViewportsChanged() {
        return this.mViewportsChanged;
    }
    
    public void updateViewports(final Viewport leftViewport, final Viewport rightViewport) {
        leftViewport.setViewport(Math.round(this.mLeftEyeViewport.x * this.mXPxPerTanAngle * this.mResolutionScale), Math.round(this.mLeftEyeViewport.y * this.mYPxPerTanAngle * this.mResolutionScale), Math.round(this.mLeftEyeViewport.width * this.mXPxPerTanAngle * this.mResolutionScale), Math.round(this.mLeftEyeViewport.height * this.mYPxPerTanAngle * this.mResolutionScale));
        rightViewport.setViewport(Math.round(this.mRightEyeViewport.x * this.mXPxPerTanAngle * this.mResolutionScale), Math.round(this.mRightEyeViewport.y * this.mYPxPerTanAngle * this.mResolutionScale), Math.round(this.mRightEyeViewport.width * this.mXPxPerTanAngle * this.mResolutionScale), Math.round(this.mRightEyeViewport.height * this.mYPxPerTanAngle * this.mResolutionScale));
        this.mViewportsChanged = false;
    }
    
    private void updateTextureAndDistortionMesh() {
        final ScreenParams screen = this.mHmd.getScreenParams();
        final CardboardDeviceParams cdp = this.mHmd.getCardboardDeviceParams();
        if (this.mProgramHolder == null) {
            this.mProgramHolder = this.createProgramHolder();
        }
        if (this.mProgramHolderAberration == null) {
            this.mProgramHolderAberration = (ProgramHolderAberration)this.createProgramHolder(true);
        }
        final float textureWidthTanAngle = this.mLeftEyeViewport.width + this.mRightEyeViewport.width;
        final float textureHeightTanAngle = Math.max(this.mLeftEyeViewport.height, this.mRightEyeViewport.height);
        final int[] maxTextureSize = { 0 };
        GLES20.glGetIntegerv(3379, maxTextureSize, 0);
        final int textureWidthPx = Math.min(Math.round(textureWidthTanAngle * this.mXPxPerTanAngle), maxTextureSize[0]);
        final int textureHeightPx = Math.min(Math.round(textureHeightTanAngle * this.mYPxPerTanAngle), maxTextureSize[0]);
        float xEyeOffsetTanAngleScreen = (screen.getWidthMeters() / 2.0f - cdp.getInterLensDistance() / 2.0f) / this.mMetersPerTanAngle;
        final float yEyeOffsetTanAngleScreen = (cdp.getVerticalDistanceToLensCenter() - screen.getBorderSizeMeters()) / this.mMetersPerTanAngle;
        this.mLeftEyeDistortionMesh = this.createDistortionMesh(this.mLeftEyeViewport, textureWidthTanAngle, textureHeightTanAngle, xEyeOffsetTanAngleScreen, yEyeOffsetTanAngleScreen);
        xEyeOffsetTanAngleScreen = screen.getWidthMeters() / this.mMetersPerTanAngle - xEyeOffsetTanAngleScreen;
        this.mRightEyeDistortionMesh = this.createDistortionMesh(this.mRightEyeViewport, textureWidthTanAngle, textureHeightTanAngle, xEyeOffsetTanAngleScreen, yEyeOffsetTanAngleScreen);
        this.setupRenderTextureAndRenderbuffer(textureWidthPx, textureHeightPx);
        this.mFovsChanged = false;
    }
    
    private EyeViewport initViewportForEye(final FieldOfView fov, final float xOffset) {
        final float left = (float)Math.tan(Math.toRadians(fov.getLeft()));
        final float right = (float)Math.tan(Math.toRadians(fov.getRight()));
        final float bottom = (float)Math.tan(Math.toRadians(fov.getBottom()));
        final float top = (float)Math.tan(Math.toRadians(fov.getTop()));
        final EyeViewport vp = new EyeViewport();
        vp.x = xOffset;
        vp.y = 0.0f;
        vp.width = left + right;
        vp.height = bottom + top;
        vp.eyeX = left + xOffset;
        vp.eyeY = bottom;
        return vp;
    }
    
    private DistortionMesh createDistortionMesh(final EyeViewport eyeViewport, final float textureWidthTanAngle, final float textureHeightTanAngle, final float xEyeOffsetTanAngleScreen, final float yEyeOffsetTanAngleScreen) {
        return new DistortionMesh(this.mHmd.getCardboardDeviceParams().getDistortion(), this.mHmd.getCardboardDeviceParams().getDistortion(), this.mHmd.getCardboardDeviceParams().getDistortion(), this.mHmd.getScreenParams().getWidthMeters() / this.mMetersPerTanAngle, this.mHmd.getScreenParams().getHeightMeters() / this.mMetersPerTanAngle, xEyeOffsetTanAngleScreen, yEyeOffsetTanAngleScreen, textureWidthTanAngle, textureHeightTanAngle, eyeViewport.eyeX, eyeViewport.eyeY, eyeViewport.x, eyeViewport.y, eyeViewport.width, eyeViewport.height);
    }
    
    private void renderDistortionMesh(final DistortionMesh mesh, final int textureId) {
        ProgramHolder holder;
        if (this.mChromaticAberrationCorrectionEnabled) {
            holder = this.mProgramHolderAberration;
        }
        else {
            holder = this.mProgramHolder;
        }
        GLES20.glBindBuffer(34962, mesh.mArrayBufferId);
        GLES20.glVertexAttribPointer(holder.aPosition, 2, 5126, false, 36, 0 * 4);
        GLES20.glEnableVertexAttribArray(holder.aPosition);
        GLES20.glVertexAttribPointer(holder.aVignette, 1, 5126, false, 36, 2 * 4);
        GLES20.glEnableVertexAttribArray(holder.aVignette);
        GLES20.glVertexAttribPointer(holder.aBlueTextureCoord, 2, 5126, false, 36, 7 * 4);
        GLES20.glEnableVertexAttribArray(holder.aBlueTextureCoord);
        if (this.mChromaticAberrationCorrectionEnabled) {
            GLES20.glVertexAttribPointer(((ProgramHolderAberration)holder).aRedTextureCoord, 2, 5126, false, 36, 3 * 4);
            GLES20.glEnableVertexAttribArray(((ProgramHolderAberration)holder).aRedTextureCoord);
            GLES20.glVertexAttribPointer(((ProgramHolderAberration)holder).aGreenTextureCoord, 2, 5126, false, 36, 5 * 4);
            GLES20.glEnableVertexAttribArray(((ProgramHolderAberration)holder).aGreenTextureCoord);
        }
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, textureId);
        GLES20.glUniform1i(this.mProgramHolder.uTextureSampler, 0);
        GLES20.glUniform1f(this.mProgramHolder.uTextureCoordScale, this.mResolutionScale);
        GLES20.glBindBuffer(34963, mesh.mElementBufferId);
        GLES20.glDrawElements(5, mesh.nIndices, 5123, 0);
    }
    
    private float computeDistortionScale(final Distortion distortion, final float screenWidthM, final float interpupillaryDistanceM) {
        return distortion.distortionFactor((screenWidthM / 2.0f - interpupillaryDistanceM / 2.0f) / (screenWidthM / 4.0f));
    }
    
    private int createTexture(final int width, final int height, final int textureFormat, final int textureType) {
        final int[] textureIds = { 0 };
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(3553, textureIds[0]);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexImage2D(3553, 0, textureFormat, width, height, 0, textureFormat, textureType, (Buffer)null);
        return textureIds[0];
    }
    
    private int setupRenderTextureAndRenderbuffer(final int width, final int height) {
        if (this.mTextureId != -1) {
            GLES20.glDeleteTextures(1, new int[] { this.mTextureId }, 0);
        }
        if (this.mRenderbufferId != -1) {
            GLES20.glDeleteRenderbuffers(1, new int[] { this.mRenderbufferId }, 0);
        }
        if (this.mFramebufferId != -1) {
            GLES20.glDeleteFramebuffers(1, new int[] { this.mFramebufferId }, 0);
        }
        this.mTextureId = this.createTexture(width, height, this.mTextureFormat, this.mTextureType);
        this.mTextureFormatChanged = false;
        this.checkGlError("setupRenderTextureAndRenderbuffer: create texture");
        final int[] renderbufferIds = { 0 };
        GLES20.glGenRenderbuffers(1, renderbufferIds, 0);
        GLES20.glBindRenderbuffer(36161, renderbufferIds[0]);
        GLES20.glRenderbufferStorage(36161, 33189, width, height);
        this.mRenderbufferId = renderbufferIds[0];
        this.checkGlError("setupRenderTextureAndRenderbuffer: create renderbuffer");
        final int[] framebufferIds = { 0 };
        GLES20.glGenFramebuffers(1, framebufferIds, 0);
        GLES20.glBindFramebuffer(36160, framebufferIds[0]);
        this.mFramebufferId = framebufferIds[0];
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, this.mTextureId, 0);
        GLES20.glFramebufferRenderbuffer(36160, 36096, 36161, renderbufferIds[0]);
        final int status = GLES20.glCheckFramebufferStatus(36160);
        if (status != 36053) {
            final String s = "Framebuffer is not complete: ";
            final String value = String.valueOf(Integer.toHexString(status));
            throw new RuntimeException((value.length() != 0) ? s.concat(value) : new String(s));
        }
        GLES20.glBindFramebuffer(36160, 0);
        return framebufferIds[0];
    }
    
    private int loadShader(final int shaderType, final String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            final int[] compiled = { 0 };
            GLES20.glGetShaderiv(shader, 35713, compiled, 0);
            if (compiled[0] == 0) {
                Log.e("DistortionRenderer", new StringBuilder(37).append("Could not compile shader ").append(shaderType).append(":").toString());
                Log.e("DistortionRenderer", GLES20.glGetShaderInfoLog(shader));
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
            this.checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            this.checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            final int[] linkStatus = { 0 };
            GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
            if (linkStatus[0] != 1) {
                Log.e("DistortionRenderer", "Could not link program: ");
                Log.e("DistortionRenderer", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
    
    private ProgramHolder createProgramHolder() {
        return this.createProgramHolder(false);
    }
    
    private ProgramHolder createProgramHolder(final boolean aberrationCorrected) {
        ProgramHolder holder;
        GLStateBackup state;
        if (aberrationCorrected) {
            holder = new ProgramHolderAberration();
            holder.program = this.createProgram("attribute vec2 aPosition;\nattribute float aVignette;\nattribute vec2 aRedTextureCoord;\nattribute vec2 aGreenTextureCoord;\nattribute vec2 aBlueTextureCoord;\nvarying vec2 vRedTextureCoord;\nvarying vec2 vBlueTextureCoord;\nvarying vec2 vGreenTextureCoord;\nvarying float vVignette;\nuniform float uTextureCoordScale;\nvoid main() {\n    gl_Position = vec4(aPosition, 0.0, 1.0);\n    vRedTextureCoord = aRedTextureCoord.xy * uTextureCoordScale;\n    vGreenTextureCoord = aGreenTextureCoord.xy * uTextureCoordScale;\n    vBlueTextureCoord = aBlueTextureCoord.xy * uTextureCoordScale;\n    vVignette = aVignette;\n}\n", "precision mediump float;\nvarying vec2 vRedTextureCoord;\nvarying vec2 vBlueTextureCoord;\nvarying vec2 vGreenTextureCoord;\nvarying float vVignette;\nuniform sampler2D uTextureSampler;\nvoid main() {\n    gl_FragColor = vVignette * vec4(texture2D(uTextureSampler, vRedTextureCoord).r,\n                    texture2D(uTextureSampler, vGreenTextureCoord).g,\n                    texture2D(uTextureSampler, vBlueTextureCoord).b, 1.0);\n}\n");
            if (holder.program == 0) {
                throw new RuntimeException("Could not create aberration-corrected program");
            }
            state = this.mGLStateBackupAberration;
        }
        else {
            holder = new ProgramHolder();
            holder.program = this.createProgram("attribute vec2 aPosition;\nattribute float aVignette;\nattribute vec2 aBlueTextureCoord;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform float uTextureCoordScale;\nvoid main() {\n    gl_Position = vec4(aPosition, 0.0, 1.0);\n    vTextureCoord = aBlueTextureCoord.xy * uTextureCoordScale;\n    vVignette = aVignette;\n}\n", "precision mediump float;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform sampler2D uTextureSampler;\nvoid main() {\n    gl_FragColor = vVignette * texture2D(uTextureSampler, vTextureCoord);\n}\n");
            if (holder.program == 0) {
                throw new RuntimeException("Could not create program");
            }
            state = this.mGLStateBackup;
        }
        holder.aPosition = GLES20.glGetAttribLocation(holder.program, "aPosition");
        this.checkGlError("glGetAttribLocation aPosition");
        if (holder.aPosition == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        state.addTrackedVertexAttribute(holder.aPosition);
        holder.aVignette = GLES20.glGetAttribLocation(holder.program, "aVignette");
        this.checkGlError("glGetAttribLocation aVignette");
        if (holder.aVignette == -1) {
            throw new RuntimeException("Could not get attrib location for aVignette");
        }
        state.addTrackedVertexAttribute(holder.aVignette);
        if (aberrationCorrected) {
            ((ProgramHolderAberration)holder).aRedTextureCoord = GLES20.glGetAttribLocation(holder.program, "aRedTextureCoord");
            this.checkGlError("glGetAttribLocation aRedTextureCoord");
            if (((ProgramHolderAberration)holder).aRedTextureCoord == -1) {
                throw new RuntimeException("Could not get attrib location for aRedTextureCoord");
            }
            ((ProgramHolderAberration)holder).aGreenTextureCoord = GLES20.glGetAttribLocation(holder.program, "aGreenTextureCoord");
            this.checkGlError("glGetAttribLocation aGreenTextureCoord");
            if (((ProgramHolderAberration)holder).aGreenTextureCoord == -1) {
                throw new RuntimeException("Could not get attrib location for aGreenTextureCoord");
            }
            state.addTrackedVertexAttribute(((ProgramHolderAberration)holder).aRedTextureCoord);
            state.addTrackedVertexAttribute(((ProgramHolderAberration)holder).aGreenTextureCoord);
        }
        holder.aBlueTextureCoord = GLES20.glGetAttribLocation(holder.program, "aBlueTextureCoord");
        this.checkGlError("glGetAttribLocation aBlueTextureCoord");
        if (holder.aBlueTextureCoord == -1) {
            throw new RuntimeException("Could not get attrib location for aBlueTextureCoord");
        }
        state.addTrackedVertexAttribute(holder.aBlueTextureCoord);
        holder.uTextureCoordScale = GLES20.glGetUniformLocation(holder.program, "uTextureCoordScale");
        this.checkGlError("glGetUniformLocation uTextureCoordScale");
        if (holder.uTextureCoordScale == -1) {
            throw new RuntimeException("Could not get attrib location for uTextureCoordScale");
        }
        holder.uTextureSampler = GLES20.glGetUniformLocation(holder.program, "uTextureSampler");
        this.checkGlError("glGetUniformLocation uTextureSampler");
        if (holder.uTextureSampler == -1) {
            throw new RuntimeException("Could not get attrib location for uTextureSampler");
        }
        return holder;
    }
    
    private void checkGlError(final String op) {
        final int error;
        if ((error = GLES20.glGetError()) != 0) {
            final String s = "DistortionRenderer";
            final String value = String.valueOf(String.valueOf(op));
            Log.e(s, new StringBuilder(21 + value.length()).append(value).append(": glError ").append(error).toString());
            final String value2 = String.valueOf(String.valueOf(op));
            throw new RuntimeException(new StringBuilder(21 + value2.length()).append(value2).append(": glError ").append(error).toString());
        }
    }
    
    private static float clamp(final float val, final float min, final float max) {
        return Math.max(min, Math.min(max, val));
    }
    
    private class ProgramHolder
    {
        public int program;
        public int aPosition;
        public int aVignette;
        public int aBlueTextureCoord;
        public int uTextureCoordScale;
        public int uTextureSampler;
    }
    
    private class ProgramHolderAberration extends ProgramHolder
    {
        public int aRedTextureCoord;
        public int aGreenTextureCoord;
    }
    
    private class EyeViewport
    {
        public float x;
        public float y;
        public float width;
        public float height;
        public float eyeX;
        public float eyeY;
        
        @Override
        public String toString() {
            return "{\n" + new StringBuilder(22).append("  x: ").append(this.x).append(",\n").toString() + new StringBuilder(22).append("  y: ").append(this.y).append(",\n").toString() + new StringBuilder(26).append("  width: ").append(this.width).append(",\n").toString() + new StringBuilder(27).append("  height: ").append(this.height).append(",\n").toString() + new StringBuilder(25).append("  eyeX: ").append(this.eyeX).append(",\n").toString() + new StringBuilder(25).append("  eyeY: ").append(this.eyeY).append(",\n").toString() + "}";
        }
    }
    
    private class DistortionMesh
    {
        private static final String TAG = "DistortionMesh";
        public static final int BYTES_PER_FLOAT = 4;
        public static final int BYTES_PER_SHORT = 2;
        public static final int COMPONENTS_PER_VERT = 9;
        public static final int DATA_STRIDE_BYTES = 36;
        public static final int DATA_POS_OFFSET = 0;
        public static final int DATA_POS_COMPONENTS = 2;
        public static final int DATA_VIGNETTE_OFFSET = 2;
        public static final int DATA_VIGNETTE_COMPONENTS = 1;
        public static final int DATA_RUV_OFFSET = 3;
        public static final int DATA_GUV_OFFSET = 5;
        public static final int DATA_BUV_OFFSET = 7;
        public static final int DATA_UV_COMPONENTS = 2;
        public static final int ROWS = 40;
        public static final int COLS = 40;
        public static final float VIGNETTE_SIZE_TAN_ANGLE = 0.05f;
        public int nIndices;
        public int mArrayBufferId;
        public int mElementBufferId;
        
        public DistortionMesh(final Distortion distortionRed, final Distortion distortionGreen, final Distortion distortionBlue, final float screenWidth, final float screenHeight, final float xEyeOffsetScreen, final float yEyeOffsetScreen, final float textureWidth, final float textureHeight, final float xEyeOffsetTexture, final float yEyeOffsetTexture, final float viewportXTexture, final float viewportYTexture, final float viewportWidthTexture, final float viewportHeightTexture) {
            super();
            this.mArrayBufferId = -1;
            this.mElementBufferId = -1;
            final float[] vertexData = new float[14400];
            short vertexOffset = 0;
            for (int row = 0; row < 40; ++row) {
                for (int col = 0; col < 40; ++col) {
                    final float uTextureBlue = col / 39.0f * (viewportWidthTexture / textureWidth) + viewportXTexture / textureWidth;
                    final float vTextureBlue = row / 39.0f * (viewportHeightTexture / textureHeight) + viewportYTexture / textureHeight;
                    final float xTexture = uTextureBlue * textureWidth - xEyeOffsetTexture;
                    final float yTexture = vTextureBlue * textureHeight - yEyeOffsetTexture;
                    final float rTexture = (float)Math.sqrt(xTexture * xTexture + yTexture * yTexture);
                    final float textureToScreenBlue = (rTexture > 0.0f) ? (distortionBlue.distortInverse(rTexture) / rTexture) : 1.0f;
                    final float xScreen = xTexture * textureToScreenBlue;
                    final float yScreen = yTexture * textureToScreenBlue;
                    final float uScreen = (xScreen + xEyeOffsetScreen) / screenWidth;
                    final float vScreen = (yScreen + yEyeOffsetScreen) / screenHeight;
                    final float rScreen = rTexture * textureToScreenBlue;
                    final float screenToTextureGreen = (rScreen > 0.0f) ? distortionGreen.distortionFactor(rScreen) : 1.0f;
                    final float uTextureGreen = (xScreen * screenToTextureGreen + xEyeOffsetTexture) / textureWidth;
                    final float vTextureGreen = (yScreen * screenToTextureGreen + yEyeOffsetTexture) / textureHeight;
                    final float screenToTextureRed = (rScreen > 0.0f) ? distortionRed.distortionFactor(rScreen) : 1.0f;
                    final float uTextureRed = (xScreen * screenToTextureRed + xEyeOffsetTexture) / textureWidth;
                    final float vTextureRed = (yScreen * screenToTextureRed + yEyeOffsetTexture) / textureHeight;
                    final float vignetteSizeTexture = 0.05f / textureToScreenBlue;
                    final float dxTexture = xTexture + xEyeOffsetTexture - clamp(xTexture + xEyeOffsetTexture, viewportXTexture + vignetteSizeTexture, viewportXTexture + viewportWidthTexture - vignetteSizeTexture);
                    final float dyTexture = yTexture + yEyeOffsetTexture - clamp(yTexture + yEyeOffsetTexture, viewportYTexture + vignetteSizeTexture, viewportYTexture + viewportHeightTexture - vignetteSizeTexture);
                    final float drTexture = (float)Math.sqrt(dxTexture * dxTexture + dyTexture * dyTexture);
                    float vignette;
                    if (DistortionRenderer.this.mVignetteEnabled) {
                        vignette = 1.0f - clamp(drTexture / vignetteSizeTexture, 0.0f, 1.0f);
                    }
                    else {
                        vignette = 1.0f;
                    }
                    vertexData[vertexOffset + 0] = 2.0f * uScreen - 1.0f;
                    vertexData[vertexOffset + 1] = 2.0f * vScreen - 1.0f;
                    vertexData[vertexOffset + 2] = vignette;
                    vertexData[vertexOffset + 3] = uTextureRed;
                    vertexData[vertexOffset + 4] = vTextureRed;
                    vertexData[vertexOffset + 5] = uTextureGreen;
                    vertexData[vertexOffset + 6] = vTextureGreen;
                    vertexData[vertexOffset + 7] = uTextureBlue;
                    vertexData[vertexOffset + 8] = vTextureBlue;
                    vertexOffset += 9;
                }
            }
            this.nIndices = 3158;
            final short[] indexData = new short[this.nIndices];
            short indexOffset = 0;
            vertexOffset = 0;
            for (int row2 = 0; row2 < 39; ++row2) {
                if (row2 > 0) {
                    indexData[indexOffset] = indexData[indexOffset - 1];
                    ++indexOffset;
                }
                for (int col2 = 0; col2 < 40; ++col2) {
                    if (col2 > 0) {
                        if (row2 % 2 == 0) {
                            ++vertexOffset;
                        }
                        else {
                            --vertexOffset;
                        }
                    }
                    final short[] array = indexData;
                    final short n = indexOffset;
                    ++indexOffset;
                    array[n] = vertexOffset;
                    final short[] array2 = indexData;
                    final short n2 = indexOffset;
                    ++indexOffset;
                    array2[n2] = (short)(vertexOffset + 40);
                }
                vertexOffset += 40;
            }
            final FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(vertexData).position(0);
            final ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indexData.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            indexBuffer.put(indexData).position(0);
            final int[] bufferIds = new int[2];
            GLES20.glGenBuffers(2, bufferIds, 0);
            this.mArrayBufferId = bufferIds[0];
            this.mElementBufferId = bufferIds[1];
            GLES20.glBindBuffer(34962, this.mArrayBufferId);
            GLES20.glBufferData(34962, vertexData.length * 4, (Buffer)vertexBuffer, 35044);
            GLES20.glBindBuffer(34963, this.mElementBufferId);
            GLES20.glBufferData(34963, indexData.length * 2, (Buffer)indexBuffer, 35044);
            GLES20.glBindBuffer(34962, 0);
            GLES20.glBindBuffer(34963, 0);
        }
    }
}
