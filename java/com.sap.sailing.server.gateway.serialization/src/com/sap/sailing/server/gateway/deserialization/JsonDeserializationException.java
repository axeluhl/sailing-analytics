package com.sap.sailing.server.gateway.deserialization;

import java.io.IOException;

public class JsonDeserializationException extends IOException {
	private static final long serialVersionUID = -6725762788023063937L;

	public JsonDeserializationException() {
		super();
	}

	public JsonDeserializationException(String message, Throwable innerException) {
		super(message, innerException);
	}

	public JsonDeserializationException(String message) {
		super(message);
	}

	public JsonDeserializationException(Throwable innerException) {
		super(innerException);
	}


}
