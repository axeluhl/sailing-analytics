package com.sap.sailing.server.gateway;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

public class HttpExceptionWithMessage extends HTTPException {
    private static final long serialVersionUID = 7603357870626865626L;
    private final String message;

    public HttpExceptionWithMessage(int statusCode, String message) {
        super(statusCode);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void sendError(HttpServletResponse response) throws IOException {
        response.sendError(getStatusCode(), getMessage());
    }

}
