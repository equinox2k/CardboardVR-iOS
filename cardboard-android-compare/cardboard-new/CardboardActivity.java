package com.google.vrtoolkit.cardboard;

import android.app.*;
import com.google.vrtoolkit.cardboard.sensors.*;
import android.nfc.*;
import android.content.*;
import android.os.*;
import android.view.*;

public class CardboardActivity extends Activity
{
    private static final int NAVIGATION_BAR_TIMEOUT_MS = 2000;
    private CardboardView mCardboardView;
    private MagnetSensor mMagnetSensor;
    private NfcSensor mNfcSensor;
    private SensorListener sensorListener;
    private int mVolumeKeysMode;
    
    public CardboardActivity() {
        super();
        this.sensorListener = new SensorListener();
    }
    
    public void setCardboardView(final CardboardView cardboardView) {
        this.mCardboardView = cardboardView;
        if (cardboardView == null) {
            return;
        }
        final NdefMessage tagContents = this.mNfcSensor.getTagContents();
        if (tagContents != null) {
            this.updateCardboardDeviceParams(CardboardDeviceParams.createFromNfcContents(tagContents));
        }
    }
    
    public CardboardView getCardboardView() {
        return this.mCardboardView;
    }
    
    public NfcSensor getNfcSensor() {
        return this.mNfcSensor;
    }
    
    public void setVolumeKeysMode(final int mode) {
        this.mVolumeKeysMode = mode;
    }
    
    public int getVolumeKeysMode() {
        return this.mVolumeKeysMode;
    }
    
    public boolean areVolumeKeysDisabled() {
        switch (this.mVolumeKeysMode) {
            case 0: {
                return false;
            }
            case 2: {
                return this.mNfcSensor.isDeviceInCardboard();
            }
            case 1: {
                return true;
            }
            default: {
                throw new IllegalStateException(new StringBuilder(36).append("Invalid volume keys mode ").append(this.mVolumeKeysMode).toString());
            }
        }
    }
    
    public void onInsertedIntoCardboard(final CardboardDeviceParams cardboardDeviceParams) {
        this.updateCardboardDeviceParams(cardboardDeviceParams);
    }
    
    public void onRemovedFromCardboard() {
    }
    
    public void onCardboardTrigger() {
    }
    
    protected void updateCardboardDeviceParams(final CardboardDeviceParams newParams) {
        if (this.mCardboardView != null) {
            this.mCardboardView.updateCardboardDeviceParams(newParams);
        }
    }
    
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.getWindow().addFlags(128);
        (this.mMagnetSensor = new MagnetSensor((Context)this)).setOnCardboardTriggerListener(this.sensorListener);
        (this.mNfcSensor = NfcSensor.getInstance((Context)this)).addOnCardboardNfcListener(this.sensorListener);
        this.mNfcSensor.onNfcIntent(this.getIntent());
        this.setVolumeKeysMode(2);
        if (Build.VERSION.SDK_INT < 19) {
            final Handler handler = new Handler();
            this.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener((View.OnSystemUiVisibilityChangeListener)new View.OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(final int visibility) {
                    if ((visibility & 0x2) == 0x0) {
                        handler.postDelayed((Runnable)new Runnable() {
                            @Override
                            public void run() {
                                CardboardActivity.this.setFullscreenMode();
                            }
                        }, 2000L);
                    }
                }
            });
        }
    }
    
    protected void onResume() {
        super.onResume();
        if (this.mCardboardView != null) {
            this.mCardboardView.onResume();
        }
        this.mMagnetSensor.start();
        this.mNfcSensor.onResume(this);
    }
    
    protected void onPause() {
        super.onPause();
        if (this.mCardboardView != null) {
            this.mCardboardView.onPause();
        }
        this.mMagnetSensor.stop();
        this.mNfcSensor.onPause(this);
    }
    
    protected void onDestroy() {
        this.mNfcSensor.removeOnCardboardNfcListener(this.sensorListener);
        super.onDestroy();
    }
    
    public void setContentView(final View view) {
        if (view instanceof CardboardView) {
            this.setCardboardView((CardboardView)view);
        }
        super.setContentView(view);
    }
    
    public void setContentView(final View view, final ViewGroup.LayoutParams params) {
        if (view instanceof CardboardView) {
            this.setCardboardView((CardboardView)view);
        }
        super.setContentView(view, params);
    }
    
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return ((keyCode == 24 || keyCode == 25) && this.areVolumeKeysDisabled()) || super.onKeyDown(keyCode, event);
    }
    
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        return ((keyCode == 24 || keyCode == 25) && this.areVolumeKeysDisabled()) || super.onKeyUp(keyCode, event);
    }
    
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.setFullscreenMode();
        }
    }
    
    private void setFullscreenMode() {
        this.getWindow().getDecorView().setSystemUiVisibility(5894);
    }
    
    public abstract static class VolumeKeys
    {
        public static final int NOT_DISABLED = 0;
        public static final int DISABLED = 1;
        public static final int DISABLED_WHILE_IN_CARDBOARD = 2;
    }
    
    private class SensorListener implements MagnetSensor.OnCardboardTriggerListener, NfcSensor.OnCardboardNfcListener
    {
        @Override
        public void onInsertedIntoCardboard(final CardboardDeviceParams deviceParams) {
            CardboardActivity.this.onInsertedIntoCardboard(deviceParams);
        }
        
        @Override
        public void onRemovedFromCardboard() {
            CardboardActivity.this.onRemovedFromCardboard();
        }
        
        @Override
        public void onCardboardTrigger() {
            CardboardActivity.this.onCardboardTrigger();
        }
    }
}
