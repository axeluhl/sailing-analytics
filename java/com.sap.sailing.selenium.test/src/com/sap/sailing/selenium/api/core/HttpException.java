package com.sap.sailing.selenium.api.core;

import java.util.Optional;

import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;

public class HttpException extends RuntimeException {

    private static final long serialVersionUID = -7750172668418557820L;

    private final int httpStatusCode;

    public static Optional<RuntimeException> forResponse(final ClientResponse response) {
        return forResponse(response, null);
    }

    public static Optional<RuntimeException> forResponse(final ClientResponse response, String message) {
        final int statusCode = response.getStatus();
        switch (statusCode) {
        case 200:
            return Optional.empty();
        case 401:
            return Optional.of(new Unauthorized(message));
        case 403:
            return Optional.of(new Forbidden(message));
        case 404:
            return Optional.of(new NotFound(message));
        default:
            return Optional.of(new HttpException(statusCode, message));
        }
    }

    public HttpException(final int httpStatusCode) {
        super();
        this.httpStatusCode = httpStatusCode;
    }

    public HttpException(final int httpStatusCode, final String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public void rethrow() {
        throw this;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public static final class NotFound extends HttpException {

        private static final long serialVersionUID = 1875842782488954242L;

        NotFound() {
            super(Response.Status.NOT_FOUND.getStatusCode());
        }

        NotFound(String message) {
            super(Response.Status.NOT_FOUND.getStatusCode(), message);
        }

    }

    public static final class Unauthorized extends HttpException {

        private static final long serialVersionUID = 1875842782488954242L;

        Unauthorized() {
            super(Response.Status.UNAUTHORIZED.getStatusCode());
        }

        Unauthorized(String message) {
            super(Response.Status.UNAUTHORIZED.getStatusCode(), message);
        }

    }

    public static final class Forbidden extends HttpException {

        private static final long serialVersionUID = 1875842782488954242L;

        Forbidden() {
            super(Response.Status.FORBIDDEN.getStatusCode());
        }

        Forbidden(String message) {
            super(Response.Status.FORBIDDEN.getStatusCode(), message);
        }

    }
}
