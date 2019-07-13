package de.hieder.vibro.entity;

public class ServerMsg {
    private String type;
    private String val;

    public ServerMsg(){

    }

    public ServerMsg(String type, String val) {
        this.type = type;
        this.val = val;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}