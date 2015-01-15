package com.google.vrtoolkit.cardboard.sensors;

import com.google.vrtoolkit.cardboard.sensors.internal.*;
import android.content.*;
import android.view.*;
import android.opengl.*;
import android.hardware.*;
import java.util.concurrent.*;

public class HeadTracker implements SensorEventListener
{
    private static final float DEFAULT_NECK_HORIZONTAL_OFFSET = 0.08f;
    private static final float DEFAULT_NECK_VERTICAL_OFFSET = 0.075f;
    private static final boolean DEFAULT_NECK_MODEL_ENABLED = false;
    private final Display mDisplay;
    private final float[] mEkfToHeadTracker;
    private final float[] mSensorToDisplay;
    private float mDisplayRotation;
    private final float[] mNeckModelTranslation;
    private final float[] mTmpHeadView;
    private final float[] mTmpHeadView2;
    private boolean mNeckModelEnabled;
    private volatile boolean mTracking;
    private OrientationEKF mTracker;
    private SensorEventProvider mSensorEventProvider;
    private Clock mClock;
    private long mLatestGyroEventClockTimeNs;
    private final Vector3d mGyroBias;
    private final Vector3d mLatestGyro;
    private final Vector3d mLatestAcc;
    
    public static HeadTracker createFromContext(final Context context) {
        final SensorManager sensorManager = (SensorManager)context.getSystemService("sensor");
        final Display display = ((WindowManager)context.getSystemService("window")).getDefaultDisplay();
        return new HeadTracker(new DeviceSensorLooper(sensorManager), new SystemClock(), display);
    }
    
    public HeadTracker(final SensorEventProvider sensorEventProvider, final Clock clock, final Display display) {
        super();
        this.mEkfToHeadTracker = new float[16];
        this.mSensorToDisplay = new float[16];
        this.mDisplayRotation = Float.NaN;
        this.mNeckModelTranslation = new float[16];
        this.mTmpHeadView = new float[16];
        this.mTmpHeadView2 = new float[16];
        this.mNeckModelEnabled = false;
        this.mGyroBias = new Vector3d();
        this.mLatestGyro = new Vector3d();
        this.mLatestAcc = new Vector3d();
        this.mClock = clock;
        this.mSensorEventProvider = sensorEventProvider;
        this.mTracker = new OrientationEKF();
        this.mDisplay = display;
        Matrix.setIdentityM(this.mNeckModelTranslation, 0);
        Matrix.translateM(this.mNeckModelTranslation, 0, 0.0f, -0.075f, 0.08f);
    }
    
    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() == 1) {
            this.mLatestAcc.set(event.values[0], event.values[1], event.values[2]);
            this.mTracker.processAcc(this.mLatestAcc, event.timestamp);
        }
        else if (event.sensor.getType() == 4) {
            this.mLatestGyroEventClockTimeNs = this.mClock.nanoTime();
            this.mLatestGyro.set(event.values[0], event.values[1], event.values[2]);
            Vector3d.sub(this.mLatestGyro, this.mGyroBias, this.mLatestGyro);
            this.mTracker.processGyro(this.mLatestGyro, event.timestamp);
        }
    }
    
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    }
    
    public void startTracking() {
        if (this.mTracking) {
            return;
        }
        this.mTracker.reset();
        this.mSensorEventProvider.registerListener((SensorEventListener)this);
        this.mSensorEventProvider.start();
        this.mTracking = true;
    }
    
    public void stopTracking() {
        if (!this.mTracking) {
            return;
        }
        this.mSensorEventProvider.unregisterListener((SensorEventListener)this);
        this.mSensorEventProvider.stop();
        this.mTracking = false;
    }
    
    public void setGyroBias(final float[] gyroBias) {
        if (gyroBias == null) {
            this.mGyroBias.setZero();
            return;
        }
        if (gyroBias.length != 3) {
            throw new IllegalArgumentException("Gyro bias should be an array of 3 values");
        }
        this.mGyroBias.set(gyroBias[0], gyroBias[1], gyroBias[2]);
    }
    
    public void setNeckModelEnabled(final boolean enabled) {
        this.mNeckModelEnabled = enabled;
    }
    
    public void getLastHeadView(final float[] headView, final int offset) {
        if (offset + 16 > headView.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        float rotation = 0.0f;
        switch (this.mDisplay.getRotation()) {
            case 0: {
                rotation = 0.0f;
                break;
            }
            case 1: {
                rotation = 90.0f;
                break;
            }
            case 2: {
                rotation = 180.0f;
                break;
            }
            case 3: {
                rotation = 270.0f;
                break;
            }
        }
        if (rotation != this.mDisplayRotation) {
            this.mDisplayRotation = rotation;
            Matrix.setRotateEulerM(this.mSensorToDisplay, 0, 0.0f, 0.0f, -rotation);
            Matrix.setRotateEulerM(this.mEkfToHeadTracker, 0, -90.0f, 0.0f, rotation);
        }
        synchronized (this.mTracker) {
            final double secondsSinceLastGyroEvent = TimeUnit.NANOSECONDS.toSeconds(this.mClock.nanoTime() - this.mLatestGyroEventClockTimeNs);
            final double secondsToPredictForward = secondsSinceLastGyroEvent + 0.03333333333333333;
            final double[] mat = this.mTracker.getPredictedGLMatrix(secondsToPredictForward);
            for (int i = 0; i < headView.length; ++i) {
                this.mTmpHeadView[i] = (float)mat[i];
            }
        }
        Matrix.multiplyMM(this.mTmpHeadView2, 0, this.mSensorToDisplay, 0, this.mTmpHeadView, 0);
        Matrix.multiplyMM(headView, offset, this.mTmpHeadView2, 0, this.mEkfToHeadTracker, 0);
        if (this.mNeckModelEnabled) {
            Matrix.multiplyMM(this.mTmpHeadView, 0, this.mNeckModelTranslation, 0, headView, offset);
            Matrix.translateM(headView, offset, this.mTmpHeadView, 0, 0.0f, 0.075f, 0.0f);
        }
    }
}
