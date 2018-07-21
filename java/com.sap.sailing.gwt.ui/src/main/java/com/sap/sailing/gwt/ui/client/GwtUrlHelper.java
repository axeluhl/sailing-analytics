package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.URL;
import com.sap.sse.common.util.UrlHelper;

/**
 * Tycho complains that {@link com.google.gwt} cannot be resolved when placing this in *common projects, where it would ideally
 * go (then we could also auto-determine if GWT context is present, e.g. through {@link GWT#isClient}).
 * 
 * @author Fredrik Teschke
 *
 */
public enum GwtUrlHelper implements UrlHelper {
    INSTANCE;
    
    @Override
    public String encodeQueryString(String queryString) {
        return URL.encodeQueryString(queryString);
    }

}
