package org.example.dto;

import java.io.Serializable;

public class RequestDTO implements Serializable {
    private String command;
    private String[] params;

    public RequestDTO(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
