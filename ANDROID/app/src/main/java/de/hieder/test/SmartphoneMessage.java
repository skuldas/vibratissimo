package de.hieder.test;

public class SmartphoneMessage {
    public String type;
    public String strVal;
    public int intVal;
    public int msgSession;

    public SmartphoneMessage(String type, String strVal, int intVal, int msgSession) {
        this.type = type;
        this.strVal = strVal;
        this.intVal = intVal;
        this.msgSession = msgSession;
    }

    public String getType() {
        return type;
    }

    public String getStrVal() {
        return strVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public int getMsgSession() {
        return msgSession;
    }

    public SmartphoneMessage toUpdate(String msg) {
        return new SmartphoneMessage("UPDATE", msg, -1, -1);
    }
}
