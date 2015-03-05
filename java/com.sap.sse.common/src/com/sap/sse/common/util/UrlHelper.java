package com.sap.sse.common.util;

/**
 * Context-independent interface for dealing with URLs.
 * GWT context does not offer full implementations of URL/URI. *
 */
public interface UrlHelper {
    String encodeUrl(String decodedUrlString);
}
