package com.google.vrtoolkit.cardboard.sensors;

import android.content.*;
import android.os.*;
import android.hardware.*;
import java.util.*;

public class MagnetSensor
{
    private static final String HTC_ONE_MODEL = "HTC One";
    private TriggerDetector mDetector;
    private Thread mDetectorThread;
    
    public MagnetSensor(final Context context) {
        super();
        if ("HTC One".equals(Build.MODEL)) {
            this.mDetector = new VectorTriggerDetector(context);
        }
        else {
            this.mDetector = new ThresholdTriggerDetector(context);
        }
    }
    
    public void start() {
        (this.mDetectorThread = new Thread(this.mDetector)).start();
    }
    
    public void stop() {
        if (this.mDetectorThread != null) {
            this.mDetectorThread.interrupt();
            this.mDetector.stop();
        }
    }
    
    public void setOnCardboardTriggerListener(final OnCardboardTriggerListener listener) {
        this.mDetector.setOnCardboardTriggerListener(listener, new Handler());
    }
    
    private abstract static class TriggerDetector implements Runnable, SensorEventListener
    {
        protected static final String TAG = "TriggerDetector";
        protected SensorManager mSensorManager;
        protected Sensor mMagnetometer;
        protected OnCardboardTriggerListener mListener;
        protected Handler mHandler;
        
        public TriggerDetector(final Context context) {
            super();
            this.mSensorManager = (SensorManager)context.getSystemService("sensor");
            this.mMagnetometer = this.mSensorManager.getDefaultSensor(2);
        }
        
        public synchronized void setOnCardboardTriggerListener(final OnCardboardTriggerListener listener, final Handler handler) {
            this.mListener = listener;
            this.mHandler = handler;
        }
        
        protected void handleButtonPressed() {
            synchronized (this) {
                if (this.mListener != null) {
                    this.mHandler.post((Runnable)new Runnable() {
                        @Override
                        public void run() {
                            if (TriggerDetector.this.mListener != null) {
                                TriggerDetector.this.mListener.onCardboardTrigger();
                            }
                        }
                    });
                }
            }
        }
        
        @Override
        public void run() {
            Looper.prepare();
            this.mSensorManager.registerListener((SensorEventListener)this, this.mMagnetometer, 0);
            Looper.loop();
        }
        
        public void stop() {
            this.mSensorManager.unregisterListener((SensorEventListener)this);
        }
        
        public void onSensorChanged(final SensorEvent event) {
        }
        
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        }
    }
    
    private static class ThresholdTriggerDetector extends TriggerDetector
    {
        private static final String TAG = "ThresholdTriggerDetector";
        private static final long NS_SEGMENT_SIZE = 200000000L;
        private static final long NS_WINDOW_SIZE = 400000000L;
        private static final long NS_WAIT_TIME = 350000000L;
        private long mLastFiring;
        private static int mT1;
        private static int mT2;
        private ArrayList<float[]> mSensorData;
        private ArrayList<Long> mSensorTimes;
        
        public ThresholdTriggerDetector(final Context context) {
            super(context);
            this.mLastFiring = 0L;
            this.mSensorData = new ArrayList<float[]>();
            this.mSensorTimes = new ArrayList<Long>();
        }
        
        public ThresholdTriggerDetector(final Context context, final int t1, final int t2) {
            super(context);
            this.mLastFiring = 0L;
            this.mSensorData = new ArrayList<float[]>();
            this.mSensorTimes = new ArrayList<Long>();
            ThresholdTriggerDetector.mT1 = t1;
            ThresholdTriggerDetector.mT2 = t2;
        }
        
        private void addData(final float[] values, final long time) {
            this.mSensorData.add(values);
            this.mSensorTimes.add(time);
            while (this.mSensorTimes.get(0) < time - 400000000L) {
                this.mSensorData.remove(0);
                this.mSensorTimes.remove(0);
            }
            this.evaluateModel(time);
        }
        
        private void evaluateModel(final long time) {
            if (time - this.mLastFiring < 350000000L || this.mSensorData.size() < 2) {
                return;
            }
            final float[] baseline = this.mSensorData.get(this.mSensorData.size() - 1);
            int startSecondSegment = 0;
            for (int i = 0; i < this.mSensorTimes.size(); ++i) {
                if (time - this.mSensorTimes.get(i) < 200000000L) {
                    startSecondSegment = i;
                    break;
                }
            }
            final float[] offsets = new float[this.mSensorData.size()];
            this.computeOffsets(offsets, baseline);
            final float min1 = this.computeMinimum(Arrays.copyOfRange(offsets, 0, startSecondSegment));
            final float max2 = this.computeMaximum(Arrays.copyOfRange(offsets, startSecondSegment, this.mSensorData.size()));
            if (min1 < ThresholdTriggerDetector.mT1 && max2 > ThresholdTriggerDetector.mT2) {
                this.mLastFiring = time;
                this.handleButtonPressed();
            }
        }
        
        private void computeOffsets(final float[] offsets, final float[] baseline) {
            for (int i = 0; i < this.mSensorData.size(); ++i) {
                final float[] point = this.mSensorData.get(i);
                final float[] o = { point[0] - baseline[0], point[1] - baseline[1], point[2] - baseline[2] };
                final float magnitude = (float)Math.sqrt(o[0] * o[0] + o[1] * o[1] + o[2] * o[2]);
                offsets[i] = magnitude;
            }
        }
        
        private float computeMaximum(final float[] offsets) {
            float max = Float.NEGATIVE_INFINITY;
            for (final float o : offsets) {
                max = Math.max(o, max);
            }
            return max;
        }
        
        private float computeMinimum(final float[] offsets) {
            float min = Float.POSITIVE_INFINITY;
            for (final float o : offsets) {
                min = Math.min(o, min);
            }
            return min;
        }
        
        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (event.sensor.equals(this.mMagnetometer)) {
                final float[] values = event.values;
                if (values[0] == 0.0f && values[1] == 0.0f && values[2] == 0.0f) {
                    return;
                }
                this.addData(event.values.clone(), event.timestamp);
            }
        }
        
        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        }
        
        static {
            ThresholdTriggerDetector.mT1 = 30;
            ThresholdTriggerDetector.mT2 = 130;
        }
    }
    
    private static class VectorTriggerDetector extends TriggerDetector
    {
        private static final String TAG = "ThresholdTriggerDetector";
        private static final long NS_REFRESH_TIME = 350000000L;
        private static final long NS_THROWAWAY_SIZE = 500000000L;
        private static final long NS_WAIT_SIZE = 100000000L;
        private long mLastFiring;
        private static int mXThreshold;
        private static int mYThreshold;
        private static int mZThreshold;
        private ArrayList<float[]> mSensorData;
        private ArrayList<Long> mSensorTimes;
        
        public VectorTriggerDetector(final Context context) {
            super(context);
            this.mLastFiring = 0L;
            this.mSensorData = new ArrayList<float[]>();
            this.mSensorTimes = new ArrayList<Long>();
            VectorTriggerDetector.mXThreshold = -3;
            VectorTriggerDetector.mYThreshold = 15;
            VectorTriggerDetector.mZThreshold = 6;
        }
        
        public VectorTriggerDetector(final Context context, final int xThreshold, final int yThreshold, final int zThreshold) {
            super(context);
            this.mLastFiring = 0L;
            this.mSensorData = new ArrayList<float[]>();
            this.mSensorTimes = new ArrayList<Long>();
            VectorTriggerDetector.mXThreshold = xThreshold;
            VectorTriggerDetector.mYThreshold = yThreshold;
            VectorTriggerDetector.mZThreshold = zThreshold;
        }
        
        private void addData(final float[] values, final long time) {
            this.mSensorData.add(values);
            this.mSensorTimes.add(time);
            while (this.mSensorTimes.get(0) < time - 500000000L) {
                this.mSensorData.remove(0);
                this.mSensorTimes.remove(0);
            }
            this.evaluateModel(time);
        }
        
        private void evaluateModel(final long time) {
            if (time - this.mLastFiring < 350000000L || this.mSensorData.size() < 2) {
                return;
            }
            int baseIndex = 0;
            for (int i = 1; i < this.mSensorTimes.size(); ++i) {
                if (time - this.mSensorTimes.get(i) < 100000000L) {
                    baseIndex = i;
                    break;
                }
            }
            final float[] oldValues = this.mSensorData.get(baseIndex);
            final float[] currentValues = this.mSensorData.get(this.mSensorData.size() - 1);
            if (currentValues[0] - oldValues[0] < VectorTriggerDetector.mXThreshold && currentValues[1] - oldValues[1] > VectorTriggerDetector.mYThreshold && currentValues[2] - oldValues[2] > VectorTriggerDetector.mZThreshold) {
                this.mLastFiring = time;
                this.handleButtonPressed();
            }
        }
        
        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (event.sensor.equals(this.mMagnetometer)) {
                final float[] values = event.values;
                if (values[0] == 0.0f && values[1] == 0.0f && values[2] == 0.0f) {
                    return;
                }
                this.addData(event.values.clone(), event.timestamp);
            }
        }
        
        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        }
    }
    
    public interface OnCardboardTriggerListener
    {
        void onCardboardTrigger();
    }
}
