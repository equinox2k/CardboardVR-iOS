package com.google.vrtoolkit.cardboard;

import com.google.vrtoolkit.cardboard.sensors.*;
import java.util.concurrent.*;
import android.content.*;
import com.google.vrtoolkit.cardboard.proto.*;
import android.util.*;
import android.view.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.*;
import android.opengl.*;
import javax.microedition.khronos.egl.*;

public class CardboardView extends GLSurfaceView
{
    private static final String TAG = "CardboardView";
    private RendererHelper mRendererHelper;
    private HeadTracker mHeadTracker;
    private HeadMountedDisplayManager mHmdManager;
    private UiLayer mUiLayer;
    private CountDownLatch mShutdownLatch;
    private boolean mVRMode;
    private boolean mRendererSet;
    private volatile boolean mRestoreGLStateEnabled;
    private volatile boolean mDistortionCorrectionEnabled;
    private volatile boolean mChromaticAberrationCorrectionEnabled;
    private volatile boolean mVignetteEnabled;
    
    public CardboardView(final Context context) {
        super(context);
        this.mVRMode = true;
        this.mRendererSet = false;
        this.mRestoreGLStateEnabled = true;
        this.mDistortionCorrectionEnabled = true;
        this.mChromaticAberrationCorrectionEnabled = false;
        this.mVignetteEnabled = true;
        this.init(context);
    }
    
    public CardboardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mVRMode = true;
        this.mRendererSet = false;
        this.mRestoreGLStateEnabled = true;
        this.mDistortionCorrectionEnabled = true;
        this.mChromaticAberrationCorrectionEnabled = false;
        this.mVignetteEnabled = true;
        this.init(context);
    }
    
    public void setRenderer(final Renderer renderer) {
        if (renderer == null) {
            return;
        }
        this.mRendererHelper.setRenderer(renderer);
        super.setRenderer((GLSurfaceView.Renderer)this.mRendererHelper);
        this.mRendererSet = true;
    }
    
    public void setRenderer(final StereoRenderer renderer) {
        this.setRenderer((renderer != null) ? new StereoRendererHelper(renderer) : ((Renderer)null));
    }
    
    public void getCurrentEyeParams(final HeadTransform head, final Eye leftEye, final Eye rightEye, final Eye monocular) {
        this.mRendererHelper.getCurrentEyeParams(head, leftEye, rightEye, monocular);
    }
    
    public void setVRModeEnabled(final boolean enabled) {
        this.mVRMode = enabled;
        this.mRendererHelper.setVRModeEnabled(enabled);
    }
    
    public boolean getVRMode() {
        return this.mVRMode;
    }
    
    public void setAlignmentMarkerEnabled(final boolean enabled) {
        this.mUiLayer.setAlignmentMarkerEnabled(enabled);
    }
    
    public boolean getAlignmentMarkerEnabled() {
        return this.mUiLayer.getAlignmentMarkerEnabled();
    }
    
    public void setSettingsButtonEnabled(final boolean enabled) {
        this.mUiLayer.setSettingsButtonEnabled(enabled);
    }
    
    public boolean getSettingsButtonEnabled() {
        return this.mUiLayer.getSettingsButtonEnabled();
    }
    
    public HeadMountedDisplay getHeadMountedDisplay() {
        return this.mHmdManager.getHeadMountedDisplay();
    }
    
    public void setRestoreGLStateEnabled(final boolean enabled) {
        this.mRestoreGLStateEnabled = enabled;
        this.mRendererHelper.setRestoreGLStateEnabled(enabled);
    }
    
    public boolean getRestoreGLStateEnabled() {
        return this.mRestoreGLStateEnabled;
    }
    
    public void setChromaticAberrationCorrectionEnabled(final boolean enabled) {
        this.mChromaticAberrationCorrectionEnabled = enabled;
        this.mRendererHelper.setChromaticAberrationCorrectionEnabled(enabled);
    }
    
    public boolean getChromaticAberrationCorrectionEnabled() {
        return this.mChromaticAberrationCorrectionEnabled;
    }
    
    public void setVignetteEnabled(final boolean enabled) {
        this.mVignetteEnabled = enabled;
        this.mRendererHelper.setVignetteEnabled(enabled);
    }
    
    public boolean getVignetteEnabled() {
        return this.mVignetteEnabled;
    }
    
    public void setNeckModelEnabled(final boolean enabled) {
        this.mHeadTracker.setNeckModelEnabled(enabled);
    }
    
    public void updateCardboardDeviceParams(final CardboardDeviceParams cardboardDeviceParams) {
        if (this.mHmdManager.updateCardboardDeviceParams(cardboardDeviceParams)) {
            this.mRendererHelper.setCardboardDeviceParams(this.getCardboardDeviceParams());
        }
    }
    
    public CardboardDeviceParams getCardboardDeviceParams() {
        return this.mHmdManager.getHeadMountedDisplay().getCardboardDeviceParams();
    }
    
    public void updateScreenParams(final ScreenParams screenParams) {
        if (this.mHmdManager.updateScreenParams(screenParams)) {
            this.mRendererHelper.setScreenParams(this.getScreenParams());
        }
    }
    
    public ScreenParams getScreenParams() {
        return this.mHmdManager.getHeadMountedDisplay().getScreenParams();
    }
    
    public float getInterpupillaryDistance() {
        return this.getCardboardDeviceParams().getInterLensDistance();
    }
    
    public void setDistortionCorrectionEnabled(final boolean enabled) {
        this.mDistortionCorrectionEnabled = enabled;
        this.mRendererHelper.setDistortionCorrectionEnabled(enabled);
    }
    
    public boolean getDistortionCorrectionEnabled() {
        return this.mDistortionCorrectionEnabled;
    }
    
    public void setDistortionCorrectionTextureFormat(final int textureFormat, final int textureType) {
        this.mRendererHelper.setDistortionCorrectionTextureFormat(textureFormat, textureType);
    }
    
    public void undistortTexture(final int inputTexture) {
        this.mRendererHelper.undistortTexture(inputTexture);
    }
    
    public void renderUiLayer() {
        this.mRendererHelper.renderUiLayer();
    }
    
    public void setDistortionCorrectionScale(final float scale) {
        this.mRendererHelper.setDistortionCorrectionScale(scale);
    }
    
    public void onResume() {
        this.mHmdManager.onResume();
        this.mRendererHelper.setCardboardDeviceParams(this.getCardboardDeviceParams());
        if (this.mRendererSet) {
            super.onResume();
        }
        final Phone.PhoneParams phoneParams = PhoneParams.readFromExternalStorage();
        if (phoneParams != null) {
            this.mHeadTracker.setGyroBias(phoneParams.gyroBias);
        }
        this.mHeadTracker.startTracking();
    }
    
    public void onPause() {
        this.mHmdManager.onPause();
        if (this.mRendererSet) {
            super.onPause();
        }
        this.mHeadTracker.stopTracking();
    }
    
    public void queueEvent(final Runnable r) {
        if (!this.mRendererSet) {
            r.run();
            return;
        }
        super.queueEvent(r);
    }
    
    public void setRenderer(final GLSurfaceView.Renderer renderer) {
        throw new RuntimeException("Please use the CardboardView renderer interfaces");
    }
    
    public void onDetachedFromWindow() {
        if (this.mRendererSet && this.mShutdownLatch == null) {
            this.mShutdownLatch = new CountDownLatch(1);
            this.mRendererHelper.shutdown();
            try {
                this.mShutdownLatch.await();
            }
            catch (InterruptedException e) {
                final String s = "CardboardView";
                final String s2 = "Interrupted during shutdown: ";
                final String value = String.valueOf(e.toString());
                Log.e(s, (value.length() != 0) ? s2.concat(value) : new String(s2));
            }
            this.mShutdownLatch = null;
        }
        super.onDetachedFromWindow();
    }
    
    private void init(final Context context) {
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.mHeadTracker = HeadTracker.createFromContext(context);
        this.mHmdManager = new HeadMountedDisplayManager(context);
        this.mRendererHelper = new RendererHelper();
        this.mUiLayer = new UiLayer(context);
    }
    
    public boolean onTouchEvent(final MotionEvent e) {
        return this.mUiLayer.onTouchEvent(e) || super.onTouchEvent(e);
    }
    
    private class RendererHelper implements GLSurfaceView.Renderer
    {
        private final HeadTransform mHeadTransform;
        private final Eye mMonocular;
        private final Eye mLeftEye;
        private final Eye mRightEye;
        private final float[] mLeftEyeTranslate;
        private final float[] mRightEyeTranslate;
        private Renderer mRenderer;
        private boolean mSurfaceCreated;
        private HeadMountedDisplay mHmd;
        private DistortionRenderer mDistortionRenderer;
        private boolean mVRMode;
        private boolean mDistortionCorrectionEnabled;
        private boolean mProjectionChanged;
        private boolean mInvalidSurfaceSize;
        
        public RendererHelper() {
            super();
            this.mHmd = new HeadMountedDisplay(CardboardView.this.getHeadMountedDisplay());
            this.mHeadTransform = new HeadTransform();
            this.mMonocular = new Eye(0);
            this.mLeftEye = new Eye(1);
            this.mRightEye = new Eye(2);
            this.updateFieldOfView(this.mLeftEye.getFov(), this.mRightEye.getFov());
            (this.mDistortionRenderer = new DistortionRenderer()).setRestoreGLStateEnabled(CardboardView.this.mRestoreGLStateEnabled);
            this.mDistortionRenderer.setChromaticAberrationCorrectionEnabled(CardboardView.this.mChromaticAberrationCorrectionEnabled);
            this.mDistortionRenderer.setVignetteEnabled(CardboardView.this.mVignetteEnabled);
            this.mLeftEyeTranslate = new float[16];
            this.mRightEyeTranslate = new float[16];
            this.mVRMode = CardboardView.this.mVRMode;
            this.mDistortionCorrectionEnabled = CardboardView.this.mDistortionCorrectionEnabled;
            this.mProjectionChanged = true;
        }
        
        public void setRenderer(final Renderer renderer) {
            this.mRenderer = renderer;
        }
        
        public void shutdown() {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (RendererHelper.this.mRenderer != null && RendererHelper.this.mSurfaceCreated) {
                        RendererHelper.this.mSurfaceCreated = false;
                        RendererHelper.this.mRenderer.onRendererShutdown();
                    }
                    CardboardView.this.mShutdownLatch.countDown();
                }
            });
        }
        
        public void setCardboardDeviceParams(final CardboardDeviceParams newParams) {
            final CardboardDeviceParams deviceParams = new CardboardDeviceParams(newParams);
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mHmd.setCardboardDeviceParams(deviceParams);
                    RendererHelper.this.mProjectionChanged = true;
                }
            });
        }
        
        public void setScreenParams(final ScreenParams newParams) {
            final ScreenParams screenParams = new ScreenParams(newParams);
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mHmd.setScreenParams(screenParams);
                    RendererHelper.this.mProjectionChanged = true;
                }
            });
        }
        
        public void setDistortionCorrectionEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionCorrectionEnabled = enabled;
                    RendererHelper.this.mProjectionChanged = true;
                }
            });
        }
        
        public void setDistortionCorrectionScale(final float scale) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.setResolutionScale(scale);
                }
            });
        }
        
        public void setVRModeEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (RendererHelper.this.mVRMode == enabled) {
                        return;
                    }
                    RendererHelper.this.mVRMode = enabled;
                    if (RendererHelper.this.mRenderer instanceof StereoRendererHelper) {
                        final StereoRendererHelper stereoHelper = (StereoRendererHelper)RendererHelper.this.mRenderer;
                        stereoHelper.setVRModeEnabled(enabled);
                    }
                    RendererHelper.this.mProjectionChanged = true;
                    RendererHelper.this.onSurfaceChanged(null, RendererHelper.this.mHmd.getScreenParams().getWidth(), RendererHelper.this.mHmd.getScreenParams().getHeight());
                }
            });
        }
        
        public void setRestoreGLStateEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.setRestoreGLStateEnabled(enabled);
                }
            });
        }
        
        public void setChromaticAberrationCorrectionEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.setChromaticAberrationCorrectionEnabled(enabled);
                }
            });
        }
        
        public void setVignetteEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.setVignetteEnabled(enabled);
                    RendererHelper.this.mProjectionChanged = true;
                }
            });
        }
        
        public void setDistortionCorrectionTextureFormat(final int textureFormat, final int textureType) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.setTextureFormat(textureFormat, textureType);
                }
            });
        }
        
        public void undistortTexture(final int inputTexture) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.mDistortionRenderer.undistortTexture(inputTexture);
                }
            });
        }
        
        public void renderUiLayer() {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    CardboardView.this.mUiLayer.draw();
                }
            });
        }
        
        public void getCurrentEyeParams(final HeadTransform head, final Eye leftEye, final Eye rightEye, final Eye monocular) {
            final CountDownLatch finished = new CountDownLatch(1);
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    RendererHelper.this.getFrameParams(head, leftEye, rightEye, monocular);
                    finished.countDown();
                }
            });
            try {
                finished.await();
            }
            catch (InterruptedException e) {
                final String s = "CardboardView";
                final String s2 = "Interrupted while reading frame params: ";
                final String value = String.valueOf(e.toString());
                Log.e(s, (value.length() != 0) ? s2.concat(value) : new String(s2));
            }
        }
        
        private void getFrameParams(final HeadTransform head, final Eye leftEye, final Eye rightEye, final Eye monocular) {
            final CardboardDeviceParams cdp = this.mHmd.getCardboardDeviceParams();
            final ScreenParams screen = this.mHmd.getScreenParams();
            CardboardView.this.mHeadTracker.getLastHeadView(head.getHeadView(), 0);
            final float halfInterpupillaryDistance = cdp.getInterLensDistance() * 0.5f;
            if (this.mVRMode) {
                Matrix.setIdentityM(this.mLeftEyeTranslate, 0);
                Matrix.setIdentityM(this.mRightEyeTranslate, 0);
                Matrix.translateM(this.mLeftEyeTranslate, 0, halfInterpupillaryDistance, 0.0f, 0.0f);
                Matrix.translateM(this.mRightEyeTranslate, 0, -halfInterpupillaryDistance, 0.0f, 0.0f);
                Matrix.multiplyMM(leftEye.getEyeView(), 0, this.mLeftEyeTranslate, 0, head.getHeadView(), 0);
                Matrix.multiplyMM(rightEye.getEyeView(), 0, this.mRightEyeTranslate, 0, head.getHeadView(), 0);
            }
            else {
                System.arraycopy(head.getHeadView(), 0, monocular.getEyeView(), 0, head.getHeadView().length);
            }
            if (this.mProjectionChanged) {
                monocular.getViewport().setViewport(0, 0, screen.getWidth(), screen.getHeight());
                CardboardView.this.mUiLayer.updateViewport(monocular.getViewport());
                if (!this.mVRMode) {
                    this.updateMonocularFieldOfView(monocular.getFov());
                }
                else if (this.mDistortionCorrectionEnabled) {
                    this.updateFieldOfView(leftEye.getFov(), rightEye.getFov());
                    this.mDistortionRenderer.onFovChanged(this.mHmd, leftEye.getFov(), rightEye.getFov(), this.getVirtualEyeToScreenDistance());
                }
                else {
                    this.updateUndistortedFovAndViewport();
                }
                leftEye.setProjectionChanged();
                rightEye.setProjectionChanged();
                monocular.setProjectionChanged();
                this.mProjectionChanged = false;
            }
            if (this.mDistortionCorrectionEnabled && this.mDistortionRenderer.haveViewportsChanged()) {
                this.mDistortionRenderer.updateViewports(leftEye.getViewport(), rightEye.getViewport());
            }
        }
        
        public void onDrawFrame(final GL10 gl) {
            if (this.mRenderer == null || !this.mSurfaceCreated || this.mInvalidSurfaceSize) {
                return;
            }
            this.getFrameParams(this.mHeadTransform, this.mLeftEye, this.mRightEye, this.mMonocular);
            if (this.mVRMode) {
                if (this.mDistortionCorrectionEnabled) {
                    this.mDistortionRenderer.beforeDrawFrame();
                    this.mRenderer.onDrawFrame(this.mHeadTransform, this.mLeftEye, this.mRightEye);
                    this.mDistortionRenderer.afterDrawFrame();
                }
                else {
                    this.mRenderer.onDrawFrame(this.mHeadTransform, this.mLeftEye, this.mRightEye);
                }
            }
            else {
                this.mRenderer.onDrawFrame(this.mHeadTransform, this.mMonocular, null);
            }
            this.mRenderer.onFinishFrame(this.mMonocular.getViewport());
            if (this.mVRMode) {
                CardboardView.this.mUiLayer.draw();
            }
        }
        
        public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
            if (this.mRenderer == null || !this.mSurfaceCreated) {
                return;
            }
            final ScreenParams screen = this.mHmd.getScreenParams();
            if (width != screen.getWidth() || height != screen.getHeight()) {
                if (!this.mInvalidSurfaceSize) {
                    GLES20.glClear(16384);
                    Log.w("CardboardView", new StringBuilder(124).append("Surface size ").append(width).append("x").append(height).append(" does not match the expected screen size ").append(screen.getWidth()).append("x").append(screen.getHeight()).append(". Rendering is disabled.").toString());
                }
                this.mInvalidSurfaceSize = true;
            }
            else {
                this.mInvalidSurfaceSize = false;
            }
            this.mRenderer.onSurfaceChanged(width, height);
        }
        
        public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
            if (this.mRenderer == null) {
                return;
            }
            this.mSurfaceCreated = true;
            this.mRenderer.onSurfaceCreated(config);
            CardboardView.this.mUiLayer.initializeGl();
        }
        
        private void updateFieldOfView(final FieldOfView leftEyeFov, final FieldOfView rightEyeFov) {
            final CardboardDeviceParams cdp = this.mHmd.getCardboardDeviceParams();
            final ScreenParams screen = this.mHmd.getScreenParams();
            final Distortion distortion = cdp.getDistortion();
            final float eyeToScreenDist = this.getVirtualEyeToScreenDistance();
            final float outerDist = (screen.getWidthMeters() - cdp.getInterLensDistance()) / 2.0f;
            final float innerDist = cdp.getInterLensDistance() / 2.0f;
            final float bottomDist = cdp.getVerticalDistanceToLensCenter() - screen.getBorderSizeMeters();
            final float topDist = screen.getHeightMeters() + screen.getBorderSizeMeters() - cdp.getVerticalDistanceToLensCenter();
            final float outerAngle = (float)Math.toDegrees(Math.atan(distortion.distort(outerDist / eyeToScreenDist)));
            final float innerAngle = (float)Math.toDegrees(Math.atan(distortion.distort(innerDist / eyeToScreenDist)));
            final float bottomAngle = (float)Math.toDegrees(Math.atan(distortion.distort(bottomDist / eyeToScreenDist)));
            final float topAngle = (float)Math.toDegrees(Math.atan(distortion.distort(topDist / eyeToScreenDist)));
            leftEyeFov.setLeft(Math.min(outerAngle, cdp.getLeftEyeMaxFov().getLeft()));
            leftEyeFov.setRight(Math.min(innerAngle, cdp.getLeftEyeMaxFov().getRight()));
            leftEyeFov.setBottom(Math.min(bottomAngle, cdp.getLeftEyeMaxFov().getBottom()));
            leftEyeFov.setTop(Math.min(topAngle, cdp.getLeftEyeMaxFov().getTop()));
            rightEyeFov.setLeft(leftEyeFov.getRight());
            rightEyeFov.setRight(leftEyeFov.getLeft());
            rightEyeFov.setBottom(leftEyeFov.getBottom());
            rightEyeFov.setTop(leftEyeFov.getTop());
        }
        
        private void updateMonocularFieldOfView(final FieldOfView monocularFov) {
            final ScreenParams screen = this.mHmd.getScreenParams();
            final float monocularBottomFov = 22.5f;
            final float monocularLeftFov = (float)Math.toDegrees(Math.atan(Math.tan(Math.toRadians(monocularBottomFov)) * screen.getWidthMeters() / screen.getHeightMeters()));
            monocularFov.setLeft(monocularLeftFov);
            monocularFov.setRight(monocularLeftFov);
            monocularFov.setBottom(monocularBottomFov);
            monocularFov.setTop(monocularBottomFov);
        }
        
        private void updateUndistortedFovAndViewport() {
            final ScreenParams screen = this.mHmd.getScreenParams();
            final CardboardDeviceParams cdp = this.mHmd.getCardboardDeviceParams();
            final float halfLensDistance = cdp.getInterLensDistance() / 2.0f;
            final float eyeToScreen = this.getVirtualEyeToScreenDistance();
            final float left = screen.getWidthMeters() / 2.0f - halfLensDistance;
            final float right = halfLensDistance;
            final float bottom = cdp.getVerticalDistanceToLensCenter() - screen.getBorderSizeMeters();
            final float top = screen.getBorderSizeMeters() + screen.getHeightMeters() - cdp.getVerticalDistanceToLensCenter();
            final FieldOfView leftEyeFov = this.mLeftEye.getFov();
            leftEyeFov.setLeft((float)Math.toDegrees(Math.atan2(left, eyeToScreen)));
            leftEyeFov.setRight((float)Math.toDegrees(Math.atan2(right, eyeToScreen)));
            leftEyeFov.setBottom((float)Math.toDegrees(Math.atan2(bottom, eyeToScreen)));
            leftEyeFov.setTop((float)Math.toDegrees(Math.atan2(top, eyeToScreen)));
            final FieldOfView rightEyeFov = this.mRightEye.getFov();
            rightEyeFov.setLeft(leftEyeFov.getRight());
            rightEyeFov.setRight(leftEyeFov.getLeft());
            rightEyeFov.setBottom(leftEyeFov.getBottom());
            rightEyeFov.setTop(leftEyeFov.getTop());
            this.mLeftEye.getViewport().setViewport(0, 0, screen.getWidth() / 2, screen.getHeight());
            this.mRightEye.getViewport().setViewport(screen.getWidth() / 2, 0, screen.getWidth() / 2, screen.getHeight());
        }
        
        private float getVirtualEyeToScreenDistance() {
            return this.mHmd.getCardboardDeviceParams().getScreenToLensDistance();
        }
    }
    
    private class StereoRendererHelper implements Renderer
    {
        private final StereoRenderer mStereoRenderer;
        private boolean mVRMode;
        
        public StereoRendererHelper(final StereoRenderer stereoRenderer) {
            super();
            this.mStereoRenderer = stereoRenderer;
            this.mVRMode = CardboardView.this.mVRMode;
        }
        
        public void setVRModeEnabled(final boolean enabled) {
            CardboardView.this.queueEvent(new Runnable() {
                @Override
                public void run() {
                    StereoRendererHelper.this.mVRMode = enabled;
                }
            });
        }
        
        @Override
        public void onDrawFrame(final HeadTransform head, final Eye leftEye, final Eye rightEye) {
            this.mStereoRenderer.onNewFrame(head);
            GLES20.glEnable(3089);
            leftEye.getViewport().setGLViewport();
            leftEye.getViewport().setGLScissor();
            this.mStereoRenderer.onDrawEye(leftEye);
            if (rightEye == null) {
                return;
            }
            rightEye.getViewport().setGLViewport();
            rightEye.getViewport().setGLScissor();
            this.mStereoRenderer.onDrawEye(rightEye);
        }
        
        @Override
        public void onFinishFrame(final Viewport viewport) {
            viewport.setGLViewport();
            viewport.setGLScissor();
            this.mStereoRenderer.onFinishFrame(viewport);
        }
        
        @Override
        public void onSurfaceChanged(final int width, final int height) {
            if (this.mVRMode) {
                this.mStereoRenderer.onSurfaceChanged(width / 2, height);
            }
            else {
                this.mStereoRenderer.onSurfaceChanged(width, height);
            }
        }
        
        @Override
        public void onSurfaceCreated(final EGLConfig config) {
            this.mStereoRenderer.onSurfaceCreated(config);
        }
        
        @Override
        public void onRendererShutdown() {
            this.mStereoRenderer.onRendererShutdown();
        }
    }
    
    public interface StereoRenderer
    {
        void onNewFrame(HeadTransform p0);
        
        void onDrawEye(Eye p0);
        
        void onFinishFrame(Viewport p0);
        
        void onSurfaceChanged(int p0, int p1);
        
        void onSurfaceCreated(EGLConfig p0);
        
        void onRendererShutdown();
    }
    
    public interface Renderer
    {
        void onDrawFrame(HeadTransform p0, Eye p1, Eye p2);
        
        void onFinishFrame(Viewport p0);
        
        void onSurfaceChanged(int p0, int p1);
        
        void onSurfaceCreated(EGLConfig p0);
        
        void onRendererShutdown();
    }
}
