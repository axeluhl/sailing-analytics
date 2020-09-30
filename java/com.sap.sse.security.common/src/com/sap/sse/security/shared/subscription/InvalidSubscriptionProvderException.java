package com.sap.sse.security.shared.subscription;

public class InvalidSubscriptionProvderException extends Exception {
    private static final long serialVersionUID = 1999570261811563556L;

    public InvalidSubscriptionProvderException() {
        super();
    }

    public InvalidSubscriptionProvderException(String message) {
        super(message);
    }
}
