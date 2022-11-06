package org.example.dto;

import java.io.Serializable;

public class ResponseDTO implements Serializable {
    private Object header;
    private Object body;

    public ResponseDTO(Object header, Object body) {
        this.header = header;
        this.body = body;
    }

    public Object getHeader() {
        return header;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
