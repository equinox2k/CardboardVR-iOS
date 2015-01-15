package com.google.vrtoolkit.cardboard;

import android.content.*;
import android.util.*;
import android.view.*;
import java.io.*;

public class HeadMountedDisplayManager
{
    private static final String TAG = "HeadMountedDisplayManager";
    private final HeadMountedDisplay mHmd;
    private final Context mContext;
    
    public HeadMountedDisplayManager(final Context context) {
        super();
        this.mContext = context;
        this.mHmd = new HeadMountedDisplay(this.createScreenParams(), this.createCardboardDeviceParams());
    }
    
    public HeadMountedDisplay getHeadMountedDisplay() {
        return this.mHmd;
    }
    
    public void onResume() {
        final CardboardDeviceParams deviceParams = this.createCardboardDeviceParamsFromExternalStorage();
        if (deviceParams != null && !deviceParams.equals(this.mHmd.getCardboardDeviceParams())) {
            this.mHmd.setCardboardDeviceParams(deviceParams);
            Log.i("HeadMountedDisplayManager", "Successfully read updated device params from external storage");
        }
        final ScreenParams screenParams = this.createScreenParamsFromExternalStorage(this.getDisplay());
        if (screenParams != null && !screenParams.equals(this.mHmd.getScreenParams())) {
            this.mHmd.setScreenParams(screenParams);
            Log.i("HeadMountedDisplayManager", "Successfully read updated screen params from external storage");
        }
    }
    
    public void onPause() {
    }
    
    public boolean updateCardboardDeviceParams(final CardboardDeviceParams cardboardDeviceParams) {
        if (cardboardDeviceParams == null || cardboardDeviceParams.equals(this.mHmd.getCardboardDeviceParams())) {
            return false;
        }
        this.mHmd.setCardboardDeviceParams(cardboardDeviceParams);
        this.writeCardboardParamsToExternalStorage();
        return true;
    }
    
    public boolean updateScreenParams(final ScreenParams screenParams) {
        if (screenParams == null || screenParams.equals(this.mHmd.getScreenParams())) {
            return false;
        }
        this.mHmd.setScreenParams(screenParams);
        return true;
    }
    
    private void writeCardboardParamsToExternalStorage() {
        boolean success = false;
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(ConfigUtils.getConfigFile("current_device_params")));
            success = this.mHmd.getCardboardDeviceParams().writeToOutputStream(stream);
        }
        catch (FileNotFoundException e) {}
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException ex) {}
            }
        }
        if (!success) {
            Log.e("HeadMountedDisplayManager", "Could not write Cardboard parameters to external storage.");
        }
        else {
            Log.i("HeadMountedDisplayManager", "Successfully wrote Cardboard parameters to external storage.");
        }
    }
    
    private Display getDisplay() {
        final WindowManager windowManager = (WindowManager)this.mContext.getSystemService("window");
        return windowManager.getDefaultDisplay();
    }
    
    private ScreenParams createScreenParams() {
        final Display display = this.getDisplay();
        final ScreenParams params = this.createScreenParamsFromExternalStorage(display);
        if (params != null) {
            Log.i("HeadMountedDisplayManager", "Successfully read screen params from external storage");
            return params;
        }
        return new ScreenParams(display);
    }
    
    private CardboardDeviceParams createCardboardDeviceParams() {
        CardboardDeviceParams params = this.createCardboardDeviceParamsFromExternalStorage();
        if (params != null) {
            Log.i("HeadMountedDisplayManager", "Successfully read device params from external storage");
            return params;
        }
        params = this.createCardboardDeviceParamsFromAssetFolder();
        if (params != null) {
            Log.i("HeadMountedDisplayManager", "Successfully read device params from asset folder");
            this.writeCardboardParamsToExternalStorage();
            return params;
        }
        return new CardboardDeviceParams();
    }
    
    private CardboardDeviceParams createCardboardDeviceParamsFromAssetFolder() {
        try {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(ConfigUtils.openAssetConfigFile(this.mContext.getAssets(), "current_device_params"));
                return CardboardDeviceParams.createFromInputStream(stream);
            }
            finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
        catch (FileNotFoundException e) {
            final String s = "HeadMountedDisplayManager";
            final String value = String.valueOf(String.valueOf(e));
            Log.d(s, new StringBuilder(47 + value.length()).append("Bundled Cardboard device parameters not found: ").append(value).toString());
        }
        catch (IOException e2) {
            final String s2 = "HeadMountedDisplayManager";
            final String value2 = String.valueOf(String.valueOf(e2));
            Log.e(s2, new StringBuilder(43 + value2.length()).append("Error reading config file in asset folder: ").append(value2).toString());
        }
        return null;
    }
    
    private CardboardDeviceParams createCardboardDeviceParamsFromExternalStorage() {
        try {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(ConfigUtils.getConfigFile("current_device_params")));
                return CardboardDeviceParams.createFromInputStream(stream);
            }
            finally {
                if (stream != null) {
                    try {
                        stream.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
        catch (FileNotFoundException e) {
            final String s = "HeadMountedDisplayManager";
            final String value = String.valueOf(String.valueOf(e));
            Log.d(s, new StringBuilder(44 + value.length()).append("Cardboard device parameters file not found: ").append(value).toString());
            return null;
        }
    }
    
    private ScreenParams createScreenParamsFromExternalStorage(final Display display) {
        try {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(ConfigUtils.getConfigFile("phone_params")));
                return ScreenParams.createFromInputStream(display, stream);
            }
            finally {
                if (stream != null) {
                    try {
                        stream.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
        catch (FileNotFoundException e) {
            final String s = "HeadMountedDisplayManager";
            final String value = String.valueOf(String.valueOf(e));
            Log.d(s, new StringBuilder(44 + value.length()).append("Cardboard screen parameters file not found: ").append(value).toString());
            return null;
        }
    }
}
