package com.sap.sailing.domain.igtimiadapter.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HttpContext;

public class RedirectStrategyExtractingAuthorizationCode implements RedirectStrategy {
    private static final Logger logger = Logger.getLogger(RedirectStrategyExtractingAuthorizationCode.class.getName());
    
    private final RedirectStrategy wrappedRedirectStrategy;
    
    private String code;
    
    public RedirectStrategyExtractingAuthorizationCode(RedirectStrategy wrappedRedirectStrategy) {
        this.wrappedRedirectStrategy = wrappedRedirectStrategy;
    }
    
    public String getCode() {
        return code;
    }

    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        boolean result = wrappedRedirectStrategy.isRedirected(request, response, context);
        int statusCode = response.getStatusLine().getStatusCode();
        Header locationHeader = response.getFirstHeader("location");
        if (locationHeader != null) {
            switch (statusCode) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                try {
                    String codeParameter = getCodeFromRedirect(locationHeader);
                    if (codeParameter != null) {
                        code = codeParameter;
                        logger.info("Found authorization code "+code+" in redirect URI "+locationHeader.getValue());
                        result = false; // don't actually re-direct as the re-direct URI may be a dummy only anyway
                    }
                } catch (URISyntaxException e) {
                    logger.log(Level.INFO, "Exception while trying to obtain code from redirect URI", e);
                }
                break;
            }
        }
        return result;
    }
    
    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        return wrappedRedirectStrategy.getRedirect(request, response, context);
    }
    
    private String getCodeFromRedirect(final Header locationHeader) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(locationHeader.getValue()), Charset.forName("UTF-8"));
        for (NameValuePair param : params) {
            if (param.getName().equals("code")) {
                return param.getValue();
            }
        }
        return null;
    }
}
