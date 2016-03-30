package com.sap.sse.security.ui.authentication;

/**
 * Interface for a view showing and error message.
 */
public interface ErrorMessageView {

    /**
     * Show the given error message
     * 
     * @param errorMessage
     *            the error message to show
     */
    void setErrorMessage(String errorMessage);

}
