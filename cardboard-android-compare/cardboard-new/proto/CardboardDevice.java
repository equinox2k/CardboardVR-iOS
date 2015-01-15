package com.google.vrtoolkit.cardboard.proto;

import java.io.*;
import com.google.protobuf.nano.*;

public interface CardboardDevice
{
    public static final class DeviceParams extends MessageNano
    {
        private static volatile DeviceParams[] _emptyArray;
        private int bitField0_;
        private String vendor_;
        private String model_;
        private float screenToLensDistance_;
        private float interLensDistance_;
        public float[] leftEyeFieldOfViewAngles;
        private float trayBottomToLensHeight_;
        public float[] distortionCoefficients;
        private boolean hasMagnet_;
        
        public static DeviceParams[] emptyArray() {
            if (DeviceParams._emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (DeviceParams._emptyArray == null) {
                        DeviceParams._emptyArray = new DeviceParams[0];
                    }
                }
            }
            return DeviceParams._emptyArray;
        }
        
        public String getVendor() {
            return this.vendor_;
        }
        
        public DeviceParams setVendor(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.vendor_ = value;
            this.bitField0_ |= 0x1;
            return this;
        }
        
        public boolean hasVendor() {
            return (this.bitField0_ & 0x1) != 0x0;
        }
        
        public DeviceParams clearVendor() {
            this.vendor_ = "";
            this.bitField0_ &= 0xFFFFFFFE;
            return this;
        }
        
        public String getModel() {
            return this.model_;
        }
        
        public DeviceParams setModel(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.model_ = value;
            this.bitField0_ |= 0x2;
            return this;
        }
        
        public boolean hasModel() {
            return (this.bitField0_ & 0x2) != 0x0;
        }
        
        public DeviceParams clearModel() {
            this.model_ = "";
            this.bitField0_ &= 0xFFFFFFFD;
            return this;
        }
        
        public float getScreenToLensDistance() {
            return this.screenToLensDistance_;
        }
        
        public DeviceParams setScreenToLensDistance(float value) {
            this.screenToLensDistance_ = value;
            this.bitField0_ |= 0x4;
            return this;
        }
        
        public boolean hasScreenToLensDistance() {
            return (this.bitField0_ & 0x4) != 0x0;
        }
        
        public DeviceParams clearScreenToLensDistance() {
            this.screenToLensDistance_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFFB;
            return this;
        }
        
        public float getInterLensDistance() {
            return this.interLensDistance_;
        }
        
        public DeviceParams setInterLensDistance(float value) {
            this.interLensDistance_ = value;
            this.bitField0_ |= 0x8;
            return this;
        }
        
        public boolean hasInterLensDistance() {
            return (this.bitField0_ & 0x8) != 0x0;
        }
        
        public DeviceParams clearInterLensDistance() {
            this.interLensDistance_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFF7;
            return this;
        }
        
        public float getTrayBottomToLensHeight() {
            return this.trayBottomToLensHeight_;
        }
        
        public DeviceParams setTrayBottomToLensHeight(float value) {
            this.trayBottomToLensHeight_ = value;
            this.bitField0_ |= 0x10;
            return this;
        }
        
        public boolean hasTrayBottomToLensHeight() {
            return (this.bitField0_ & 0x10) != 0x0;
        }
        
        public DeviceParams clearTrayBottomToLensHeight() {
            this.trayBottomToLensHeight_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFEF;
            return this;
        }
        
        public boolean getHasMagnet() {
            return this.hasMagnet_;
        }
        
        public DeviceParams setHasMagnet(boolean value) {
            this.hasMagnet_ = value;
            this.bitField0_ |= 0x20;
            return this;
        }
        
        public boolean hasHasMagnet() {
            return (this.bitField0_ & 0x20) != 0x0;
        }
        
        public DeviceParams clearHasMagnet() {
            this.hasMagnet_ = false;
            this.bitField0_ &= 0xFFFFFFDF;
            return this;
        }
        
        public DeviceParams() {
            super();
            this.clear();
        }
        
        public DeviceParams clear() {
            this.bitField0_ = 0;
            this.vendor_ = "";
            this.model_ = "";
            this.screenToLensDistance_ = 0.0f;
            this.interLensDistance_ = 0.0f;
            this.leftEyeFieldOfViewAngles = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.trayBottomToLensHeight_ = 0.0f;
            this.distortionCoefficients = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.hasMagnet_ = false;
            this.cachedSize = -1;
            return this;
        }
        
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int dataSize;
            if ((this.bitField0_ & 0x1) != 0x0) {
                output.writeString(1, this.vendor_);
            }
            if ((this.bitField0_ & 0x2) != 0x0) {
                output.writeString(2, this.model_);
            }
            if ((this.bitField0_ & 0x4) != 0x0) {
                output.writeFloat(3, this.screenToLensDistance_);
            }
            if ((this.bitField0_ & 0x8) != 0x0) {
                output.writeFloat(4, this.interLensDistance_);
            }
            if (this.leftEyeFieldOfViewAngles != null && this.leftEyeFieldOfViewAngles.length > 0) {
                dataSize = 4 * this.leftEyeFieldOfViewAngles.length;
                output.writeRawVarint32(42);
                output.writeRawVarint32(dataSize);
                for (int i = 0; i < this.leftEyeFieldOfViewAngles.length; ++i) {
                    output.writeFloatNoTag(this.leftEyeFieldOfViewAngles[i]);
                }
            }
            if ((this.bitField0_ & 0x10) != 0x0) {
                output.writeFloat(6, this.trayBottomToLensHeight_);
            }
            if (this.distortionCoefficients != null && this.distortionCoefficients.length > 0) {
                dataSize = 4 * this.distortionCoefficients.length;
                output.writeRawVarint32(58);
                output.writeRawVarint32(dataSize);
                for (int i = 0; i < this.distortionCoefficients.length; ++i) {
                    output.writeFloatNoTag(this.distortionCoefficients[i]);
                }
            }
            if ((this.bitField0_ & 0x20) != 0x0) {
                output.writeBool(10, this.hasMagnet_);
            }
            super.writeTo(output);
        }
        
        protected int computeSerializedSize() {
            int size;
            int dataSize;
            size = super.computeSerializedSize();
            if ((this.bitField0_ & 0x1) != 0x0) {
                size += CodedOutputByteBufferNano.computeStringSize(1, this.vendor_);
            }
            if ((this.bitField0_ & 0x2) != 0x0) {
                size += CodedOutputByteBufferNano.computeStringSize(2, this.model_);
            }
            if ((this.bitField0_ & 0x4) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(3, this.screenToLensDistance_);
            }
            if ((this.bitField0_ & 0x8) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(4, this.interLensDistance_);
            }
            if (this.leftEyeFieldOfViewAngles != null && this.leftEyeFieldOfViewAngles.length > 0) {
                dataSize = 4 * this.leftEyeFieldOfViewAngles.length;
                size += dataSize;
                size = ++size + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
            }
            if ((this.bitField0_ & 0x10) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(6, this.trayBottomToLensHeight_);
            }
            if (this.distortionCoefficients != null && this.distortionCoefficients.length > 0) {
                dataSize = 4 * this.distortionCoefficients.length;
                size += dataSize;
                size = ++size + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
            }
            if ((this.bitField0_ & 0x20) != 0x0) {
                size += CodedOutputByteBufferNano.computeBoolSize(10, this.hasMagnet_);
            }
            return size;
        }
        
        public DeviceParams mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int tag;
            int arrayLength;
            int i;
            float[] newArray;
            int length;
            int limit;
            int arrayLength2;
            int j;
            float[] newArray2;
            while (true) {
                tag = input.readTag();
                switch (tag) {
                    case 0: {
                        return this;
                    }
                    default: {
                        if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                        continue;
                    }
                    case 10: {
                        this.vendor_ = input.readString();
                        this.bitField0_ |= 0x1;
                        continue;
                    }
                    case 18: {
                        this.model_ = input.readString();
                        this.bitField0_ |= 0x2;
                        continue;
                    }
                    case 29: {
                        this.screenToLensDistance_ = input.readFloat();
                        this.bitField0_ |= 0x4;
                        continue;
                    }
                    case 37: {
                        this.interLensDistance_ = input.readFloat();
                        this.bitField0_ |= 0x8;
                        continue;
                    }
                    case 45: {
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 45);
                        i = ((this.leftEyeFieldOfViewAngles == null) ? 0 : this.leftEyeFieldOfViewAngles.length);
                        newArray = new float[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.leftEyeFieldOfViewAngles, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readFloat();
                            input.readTag();
                            ++i;
                        }
                        newArray[i] = input.readFloat();
                        this.leftEyeFieldOfViewAngles = newArray;
                        continue;
                    }
                    case 42: {
                        length = input.readRawVarint32();
                        limit = input.pushLimit(length);
                        arrayLength2 = length / 4;
                        j = ((this.leftEyeFieldOfViewAngles == null) ? 0 : this.leftEyeFieldOfViewAngles.length);
                        newArray2 = new float[j + arrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.leftEyeFieldOfViewAngles, 0, newArray2, 0, j);
                        }
                        while (j < newArray2.length) {
                            newArray2[j] = input.readFloat();
                            ++j;
                        }
                        this.leftEyeFieldOfViewAngles = newArray2;
                        input.popLimit(limit);
                        continue;
                    }
                    case 53: {
                        this.trayBottomToLensHeight_ = input.readFloat();
                        this.bitField0_ |= 0x10;
                        continue;
                    }
                    case 61: {
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 61);
                        i = ((this.distortionCoefficients == null) ? 0 : this.distortionCoefficients.length);
                        newArray = new float[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.distortionCoefficients, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readFloat();
                            input.readTag();
                            ++i;
                        }
                        newArray[i] = input.readFloat();
                        this.distortionCoefficients = newArray;
                        continue;
                    }
                    case 58: {
                        length = input.readRawVarint32();
                        limit = input.pushLimit(length);
                        arrayLength2 = length / 4;
                        j = ((this.distortionCoefficients == null) ? 0 : this.distortionCoefficients.length);
                        newArray2 = new float[j + arrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.distortionCoefficients, 0, newArray2, 0, j);
                        }
                        while (j < newArray2.length) {
                            newArray2[j] = input.readFloat();
                            ++j;
                        }
                        this.distortionCoefficients = newArray2;
                        input.popLimit(limit);
                        continue;
                    }
                    case 80: {
                        this.hasMagnet_ = input.readBool();
                        this.bitField0_ |= 0x20;
                        continue;
                    }
                }
            }
        }
        
        public static DeviceParams parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (DeviceParams)MessageNano.mergeFrom((MessageNano)new DeviceParams(), data);
        }
        
        public static DeviceParams parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new DeviceParams().mergeFrom(input);
        }
    }
}
