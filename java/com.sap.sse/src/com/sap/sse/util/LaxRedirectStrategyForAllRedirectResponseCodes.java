package com.sap.sse.util;

import java.net.URI;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class LaxRedirectStrategyForAllRedirectResponseCodes extends LaxRedirectStrategy {
    @Override
    public HttpUriRequest getRedirect(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        URI uri = getLocationURI(request, response, context);
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            return new HttpGet(uri);
        } else {
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_TEMPORARY_REDIRECT ||
                    status == HttpStatus.SC_MOVED_PERMANENTLY ||
                    status == HttpStatus.SC_MOVED_TEMPORARILY) {
                if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
                    return copyEntity(new HttpPost(uri), request);
                } else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
                    return copyEntity(new HttpPut(uri), request);
                } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                    return new HttpDelete(uri);
                } else if (method.equalsIgnoreCase(HttpTrace.METHOD_NAME)) {
                    return new HttpTrace(uri);
                } else if (method.equalsIgnoreCase(HttpOptions.METHOD_NAME)) {
                    return new HttpOptions(uri);
                } else if (method.equalsIgnoreCase(HttpPatch.METHOD_NAME)) {
                    return copyEntity(new HttpPatch(uri), request);
                }
            }
            return new HttpGet(uri);
        }
    }
    
    private HttpUriRequest copyEntity(
            final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
        if (original instanceof HttpEntityEnclosingRequest) {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }
}
