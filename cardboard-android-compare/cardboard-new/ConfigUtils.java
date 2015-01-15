package com.google.vrtoolkit.cardboard;

import android.os.*;
import android.content.res.*;
import java.io.*;

public abstract class ConfigUtils
{
    public static final String CARDBOARD_CONFIG_FOLDER = "Cardboard";
    public static final String CARDBOARD_DEVICE_PARAMS_FILE = "current_device_params";
    public static final String CARDBOARD_PHONE_PARAMS_FILE = "phone_params";
    
    public static File getConfigFile(final String filename) {
        final File configFolder = new File(Environment.getExternalStorageDirectory(), "Cardboard");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        else if (!configFolder.isDirectory()) {
            final String value = String.valueOf(String.valueOf(configFolder));
            throw new IllegalStateException(new StringBuilder(22 + value.length()).append("Folder ").append(value).append(" already exists").toString());
        }
        return new File(configFolder, filename);
    }
    
    public static InputStream openAssetConfigFile(final AssetManager assetManager, final String filename) throws IOException {
        final String assetPath = new File("Cardboard", filename).getPath();
        return assetManager.open(assetPath);
    }
}
