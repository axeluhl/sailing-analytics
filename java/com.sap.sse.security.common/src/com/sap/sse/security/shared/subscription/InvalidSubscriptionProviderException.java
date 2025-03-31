package com.sap.sse.security.shared.subscription;

public class InvalidSubscriptionProviderException extends Exception {
    private static final long serialVersionUID = 1999570261811563556L;

    public InvalidSubscriptionProviderException() {
        super();
    }

    public InvalidSubscriptionProviderException(String message) {
        super(message);
    }
}
