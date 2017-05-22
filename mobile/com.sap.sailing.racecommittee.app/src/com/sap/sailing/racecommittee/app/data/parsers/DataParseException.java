package com.sap.sailing.racecommittee.app.data.parsers;

public class DataParseException extends Exception {
	private static final long serialVersionUID = 3079074496188715438L;

	public DataParseException() { }

	public DataParseException(String detailMessage) {
		super(detailMessage);
	}

	public DataParseException(Throwable throwable) {
		super(throwable);
	}

	public DataParseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
