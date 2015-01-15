package com.google.vrtoolkit.cardboard.proto;

import java.io.*;
import com.google.protobuf.nano.*;

public interface Phone
{
    public static final class PhoneParams extends MessageNano
    {
        private static volatile PhoneParams[] _emptyArray;
        private int bitField0_;
        private float xPpi_;
        private float yPpi_;
        private float bottomBezelHeight_;
        public float[] gyroBias;
        
        public static PhoneParams[] emptyArray() {
            if (PhoneParams._emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (PhoneParams._emptyArray == null) {
                        PhoneParams._emptyArray = new PhoneParams[0];
                    }
                }
            }
            return PhoneParams._emptyArray;
        }
        
        public float getXPpi() {
            return this.xPpi_;
        }
        
        public PhoneParams setXPpi(float value) {
            this.xPpi_ = value;
            this.bitField0_ |= 0x1;
            return this;
        }
        
        public boolean hasXPpi() {
            return (this.bitField0_ & 0x1) != 0x0;
        }
        
        public PhoneParams clearXPpi() {
            this.xPpi_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFFE;
            return this;
        }
        
        public float getYPpi() {
            return this.yPpi_;
        }
        
        public PhoneParams setYPpi(float value) {
            this.yPpi_ = value;
            this.bitField0_ |= 0x2;
            return this;
        }
        
        public boolean hasYPpi() {
            return (this.bitField0_ & 0x2) != 0x0;
        }
        
        public PhoneParams clearYPpi() {
            this.yPpi_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFFD;
            return this;
        }
        
        public float getBottomBezelHeight() {
            return this.bottomBezelHeight_;
        }
        
        public PhoneParams setBottomBezelHeight(float value) {
            this.bottomBezelHeight_ = value;
            this.bitField0_ |= 0x4;
            return this;
        }
        
        public boolean hasBottomBezelHeight() {
            return (this.bitField0_ & 0x4) != 0x0;
        }
        
        public PhoneParams clearBottomBezelHeight() {
            this.bottomBezelHeight_ = 0.0f;
            this.bitField0_ &= 0xFFFFFFFB;
            return this;
        }
        
        public PhoneParams() {
            super();
            this.clear();
        }
        
        public PhoneParams clear() {
            this.bitField0_ = 0;
            this.xPpi_ = 0.0f;
            this.yPpi_ = 0.0f;
            this.bottomBezelHeight_ = 0.0f;
            this.gyroBias = WireFormatNano.EMPTY_FLOAT_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        public void writeTo(CodedOutputByteBufferNano output) throws IOException {
            int dataSize;
            if ((this.bitField0_ & 0x1) != 0x0) {
                output.writeFloat(1, this.xPpi_);
            }
            if ((this.bitField0_ & 0x2) != 0x0) {
                output.writeFloat(2, this.yPpi_);
            }
            if ((this.bitField0_ & 0x4) != 0x0) {
                output.writeFloat(3, this.bottomBezelHeight_);
            }
            if (this.gyroBias != null && this.gyroBias.length > 0) {
                dataSize = 4 * this.gyroBias.length;
                output.writeRawVarint32(34);
                output.writeRawVarint32(dataSize);
                for (int i = 0; i < this.gyroBias.length; ++i) {
                    output.writeFloatNoTag(this.gyroBias[i]);
                }
            }
            super.writeTo(output);
        }
        
        protected int computeSerializedSize() {
            int size;
            int dataSize;
            size = super.computeSerializedSize();
            if ((this.bitField0_ & 0x1) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(1, this.xPpi_);
            }
            if ((this.bitField0_ & 0x2) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(2, this.yPpi_);
            }
            if ((this.bitField0_ & 0x4) != 0x0) {
                size += CodedOutputByteBufferNano.computeFloatSize(3, this.bottomBezelHeight_);
            }
            if (this.gyroBias != null && this.gyroBias.length > 0) {
                dataSize = 4 * this.gyroBias.length;
                size += dataSize;
                size = ++size + CodedOutputByteBufferNano.computeRawVarint32Size(dataSize);
            }
            return size;
        }
        
        public PhoneParams mergeFrom(CodedInputByteBufferNano input) throws IOException {
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
                    case 13: {
                        this.xPpi_ = input.readFloat();
                        this.bitField0_ |= 0x1;
                        continue;
                    }
                    case 21: {
                        this.yPpi_ = input.readFloat();
                        this.bitField0_ |= 0x2;
                        continue;
                    }
                    case 29: {
                        this.bottomBezelHeight_ = input.readFloat();
                        this.bitField0_ |= 0x4;
                        continue;
                    }
                    case 37: {
                        arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 37);
                        i = ((this.gyroBias == null) ? 0 : this.gyroBias.length);
                        newArray = new float[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.gyroBias, 0, newArray, 0, i);
                        }
                        while (i < newArray.length - 1) {
                            newArray[i] = input.readFloat();
                            input.readTag();
                            ++i;
                        }
                        newArray[i] = input.readFloat();
                        this.gyroBias = newArray;
                        continue;
                    }
                    case 34: {
                        length = input.readRawVarint32();
                        limit = input.pushLimit(length);
                        arrayLength2 = length / 4;
                        j = ((this.gyroBias == null) ? 0 : this.gyroBias.length);
                        newArray2 = new float[j + arrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.gyroBias, 0, newArray2, 0, j);
                        }
                        while (j < newArray2.length) {
                            newArray2[j] = input.readFloat();
                            ++j;
                        }
                        this.gyroBias = newArray2;
                        input.popLimit(limit);
                        continue;
                    }
                }
            }
        }
        
        public static PhoneParams parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (PhoneParams)MessageNano.mergeFrom((MessageNano)new PhoneParams(), data);
        }
        
        public static PhoneParams parseFrom(CodedInputByteBufferNano input) throws IOException {
            return new PhoneParams().mergeFrom(input);
        }
    }
}
