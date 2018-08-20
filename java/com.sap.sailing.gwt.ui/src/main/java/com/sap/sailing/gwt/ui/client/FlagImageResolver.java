package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FlagImageResolver {
    public static void get(AsyncCallback<FlagImageResolver> callback) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess(FlagImageResolverImpl.get());
            }
            
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }
        });
    }

    ImageResource getFlagImageResource(String twoLetterIsoCountryCode);
    
    SafeUri getFlagImageUri(String flagImageUrl, String twoLetterIsoCountryCode);

    ImageResource getEmptyFlagImageResource();
}
