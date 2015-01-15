package com.google.vrtoolkit.cardboard;

import com.google.vrtoolkit.cardboard.proto.*;
import java.nio.*;
import android.util.*;
import com.google.protobuf.nano.*;
import java.io.*;

public class PhoneParams
{
    private static final String TAG;
    private static final int STREAM_SENTINEL = 779508118;
    
    static Phone.PhoneParams readFromInputStream(final InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            final ByteBuffer header = ByteBuffer.allocate(8);
            if (inputStream.read(header.array(), 0, header.array().length) == -1) {
                Log.e(PhoneParams.TAG, "Error parsing param record: end of stream.");
                return null;
            }
            final int sentinel = header.getInt();
            final int length = header.getInt();
            if (sentinel != 779508118) {
                Log.e(PhoneParams.TAG, "Error parsing param record: incorrect sentinel.");
                return null;
            }
            final byte[] protoBytes = new byte[length];
            if (inputStream.read(protoBytes, 0, protoBytes.length) == -1) {
                Log.e(PhoneParams.TAG, "Error parsing param record: end of stream.");
                return null;
            }
            return (Phone.PhoneParams)MessageNano.mergeFrom((MessageNano)new Phone.PhoneParams(), protoBytes);
        }
        catch (InvalidProtocolBufferNanoException e) {
            final String tag = PhoneParams.TAG;
            final String s = "Error parsing protocol buffer: ";
            final String value = String.valueOf(e.toString());
            Log.w(tag, (value.length() != 0) ? s.concat(value) : new String(s));
        }
        catch (IOException e2) {
            final String tag2 = PhoneParams.TAG;
            final String s2 = "Error reading Cardboard parameters: ";
            final String value2 = String.valueOf(e2.toString());
            Log.w(tag2, (value2.length() != 0) ? s2.concat(value2) : new String(s2));
        }
        return null;
    }
    
    static Phone.PhoneParams readFromExternalStorage() {
        try {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(ConfigUtils.getConfigFile("phone_params")));
                return readFromInputStream(stream);
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
            final String tag = PhoneParams.TAG;
            final String value = String.valueOf(String.valueOf(e));
            Log.d(tag, new StringBuilder(43 + value.length()).append("Cardboard phone parameters file not found: ").append(value).toString());
            return null;
        }
    }
    
    static {
        TAG = PhoneParams.class.getSimpleName();
    }
}
