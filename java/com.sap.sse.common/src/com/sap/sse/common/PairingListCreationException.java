package com.sap.sse.common;

import java.io.Serializable;

public class PairingListCreationException extends Exception implements Serializable {
    private static final long serialVersionUID = 3189130662288098810L;

    public PairingListCreationException() {
        super();
    }

    public PairingListCreationException(String message) {
        super(message);
    }
}
