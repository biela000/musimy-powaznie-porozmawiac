package com.jtjmpm;

public class WsErrorMessage extends WsMessage {
    public String name;
    public String description;

    public WsErrorMessage(String name, String description) {
        super("ERROR");
        this.name = name;
        this.description = description;
    }
}
