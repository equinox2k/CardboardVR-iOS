package com.google.vrtoolkit.cardboard;

public class HeadMountedDisplay
{
    private ScreenParams mScreen;
    private CardboardDeviceParams mCardboardDevice;
    
    public HeadMountedDisplay(final ScreenParams screenParams, final CardboardDeviceParams cardboardDevice) {
        super();
        this.mScreen = screenParams;
        this.mCardboardDevice = cardboardDevice;
    }
    
    public HeadMountedDisplay(final HeadMountedDisplay hmd) {
        super();
        this.mScreen = new ScreenParams(hmd.mScreen);
        this.mCardboardDevice = new CardboardDeviceParams(hmd.mCardboardDevice);
    }
    
    public void setScreenParams(final ScreenParams screen) {
        this.mScreen = new ScreenParams(screen);
    }
    
    public ScreenParams getScreenParams() {
        return this.mScreen;
    }
    
    public void setCardboardDeviceParams(final CardboardDeviceParams cardboardDeviceParams) {
        this.mCardboardDevice = new CardboardDeviceParams(cardboardDeviceParams);
    }
    
    public CardboardDeviceParams getCardboardDeviceParams() {
        return this.mCardboardDevice;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeadMountedDisplay)) {
            return false;
        }
        final HeadMountedDisplay o = (HeadMountedDisplay)other;
        return this.mScreen.equals(o.mScreen) && this.mCardboardDevice.equals(o.mCardboardDevice);
    }
}
