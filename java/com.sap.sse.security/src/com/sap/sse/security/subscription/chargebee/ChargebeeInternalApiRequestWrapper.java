package com.sap.sse.security.subscription.chargebee;

import com.chargebee.ApiResponse;
import com.chargebee.ListResult;
import com.chargebee.Result;
import com.chargebee.internal.ListRequest;
import com.chargebee.internal.Request;

/**
 * <p>
 * Wrapper for internal Chargebee request: Request and ListRequest. These two request classes don't have common request
 * interface methods, so this wrapper class is necessary.
 * </p>
 * <p>
 * Only one instance of those two request types is present in a wrapper instance at a time.
 * </p>
 * <p>
 * This class makes convenience for implementation of {@code ChargebeeApiRequest}
 * </p>
 */
public class ChargebeeInternalApiRequestWrapper {
    private Request<?> request;
    private ListRequest<?> listRequest;
    private ListResult listResult;
    private Result result;

    public ChargebeeInternalApiRequestWrapper(Request<?> request) {
        this.request = request;
    }

    public ChargebeeInternalApiRequestWrapper(ListRequest<?> listRequest) {
        this.listRequest = listRequest;
    }

    public boolean isListRequest() {
        return listRequest != null;
    }

    public void request() throws Exception {
        if (listRequest != null) {
            listResult = listRequest.request();
        } else if (request != null) {
            result = request.request();
        } else {
            throw new Exception("No request found.");
        }
    }

    public ListResult getListResult() {
        return listResult;
    }

    public Result getResult() {
        return result;
    }

    public ApiResponse getResponse() {
        return isListRequest() ? listResult : result;
    }
}
