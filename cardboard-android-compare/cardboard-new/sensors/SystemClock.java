package com.google.vrtoolkit.cardboard.sensors;

public class SystemClock implements Clock
{
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
