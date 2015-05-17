package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class ErrorPlace extends AbstractBasePlace {
    
    /**
     * Place provided for reload action
     */
    private Place comingFrom;

    /**
     * Custom error messages
     */
    private String errorMessage;

    /**
     * Detail error message
     */
    private final String errorMessageDetail;

    public ErrorPlace(String errorMessageDetail) {
        this.errorMessageDetail = errorMessageDetail;
    }
    
    /**
     * Exception ocurred
     */
    private Throwable exception;

    public Place getComingFrom() {
        return comingFrom;
    }

    public void setComingFrom(Place comingFrom) {
        this.comingFrom = comingFrom;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorMessageDetail() {
        return errorMessageDetail;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public static class Tokenizer implements PlaceTokenizer<ErrorPlace> {
        @Override
        public String getToken(ErrorPlace place) {
            return null;
        }

        @Override
        public ErrorPlace getPlace(String token) {
            return new ErrorPlace("");
        }
    }

    public boolean hasCustomErrorMessages() {

        return errorMessage != null && !errorMessage.isEmpty();
    }
}
