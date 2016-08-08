package com.sap.sse.gwt.settings;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

public class UrlBuilderUtil {
    
    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParameters() {
        final UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
        for(String parameterName : Window.Location.getParameterMap().keySet()) {
            if(!"gwt.codesvr".equals(parameterName)
                    && !"locale".equals(parameterName)) {
                urlBuilder.removeParameter(parameterName);
            }
        }
        urlBuilder.setHash(null);
        return urlBuilder;
    }
    
    public static UrlBuilder createUrlBuilderFromCurrentLocationWithCleanParameters(String path) {
        final UrlBuilder urlBuilder = createUrlBuilderFromCurrentLocationWithCleanParameters();
        urlBuilder.setPath(path);
        return urlBuilder;
    }
}
