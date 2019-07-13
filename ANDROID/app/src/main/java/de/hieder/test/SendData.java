package de.hieder.test;

public class SendData{

    public static enum SendDataType{
        SLEEP,
        MODE,
        VALUE
    }

    private SendDataType type;
    private byte[] data;
    private int sleep;

    public SendData(SendDataType type, byte[] data) {
        this.type = type;
        this.data = data;
        this.sleep = 0;
    }

    public SendData(SendDataType type, int sleep) {
        this.type = type;
        this.data = null;
        this.sleep = sleep;
    }

    public SendDataType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public int getSleep() {
        return sleep;
    }
}
