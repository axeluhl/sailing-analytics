package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sse.gwt.client.DefaultErrorReporter;
import com.sap.sse.gwt.client.StringMessages;

/**
 * Interface to use when the behavior of a implementation depends on the certain side conditions on the backend which
 * can change over time.
 * 
 * @author Georg Herdt
 *
 */
public interface HasAvailabilityCheck {
    /**
     * Check if related backend functionality is currently available. A callback is used here to allow asynchronous GWT RPC
     * backend calls for checking the availability.
     * 
     * @param callback
     *            Callback will be issued with the outcome of the functionality check.
     */
    void checkBackendAvailability(Consumer<Boolean> callback);

    /**
     * Helper method to do the backend check.
     * 
     * @param sailingServiceWrite
     *            backend service to be called
     * @param callback
     *            the callback to handle out come of the check
     * @param stringMessages
     *            message bundle for popup dialog
     */
    static void validateBackendAvailabilityAndExecuteBusinessLogic(SailingServiceWriteAsync sailingServiceWrite,
            Consumer<Boolean> callback, StringMessages stringMessages) {
        sailingServiceWrite.getServerConfiguration(new AsyncCallback<ServerConfigurationDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                if (has5xxResponse(caught)) {
                    DefaultErrorReporter.reportMasterTemporarilyUnavailable(stringMessages);
                    callback.accept(false);
                } else {
                    // let standard error handling process this
                    callback.accept(true);
                }
            }

            @Override
            public void onSuccess(ServerConfigurationDTO result) {
                // nothing to do. validation query has succeeded. pass on to callback
                callback.accept(true);
            }
        });
    }

    /**
     * Helper to check if the current runtime type of the Throwable is a StatusCode exception and inspect the status code in it.
     * @param caught Throwable to inspect
     * @return Returns true when a status code can be found at its value is within 500 and 599.
     */
    static boolean has5xxResponse(Throwable caught) {
        boolean has5xx = false;
        if (caught instanceof StatusCodeException) {
            StatusCodeException sce = (StatusCodeException) caught;
            if (sce.getStatusCode() >= 500 && sce.getStatusCode() < 600) {
                has5xx = true;
            }
        }
        return has5xx;
    }
}
