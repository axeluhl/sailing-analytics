package com.sap.sailing.gwt.home.shared.places.error;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class ErrorPlace extends AbstractBasePlace implements HasMobileVersion {
    
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

    /**
     * Flag to determine whether this is the initial {@link ErrorPlace} or a reloaded one.
     */
    private final boolean reloadedError;

    public ErrorPlace(String errorMessageDetail) {
        this(false, errorMessageDetail);
    }

    private ErrorPlace(final boolean reloadedError, final String errorMessageDetail) {
        this.errorMessageDetail = errorMessageDetail;
        this.reloadedError = reloadedError;
    }

    /**
     * Exception ocurred
     */
    private Throwable exception;

    public boolean isReloadedError() {
        return reloadedError;
    }

    public String getErrorMessageDetail() {
        return errorMessageDetail;
    }

    public Place getComingFrom() {
        return comingFrom;
    }

    public void setComingFrom(Place comingFrom) {
        this.comingFrom = comingFrom;
    }

    public String getErrorMessage() {
        return errorMessage;
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
            return "";
        }

        @Override
        public ErrorPlace getPlace(String token) {
            return new ErrorPlace(true, null);
        }
    }

    public boolean hasCustomErrorMessages() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}
