package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FlagImageResolver {
    static final Template FLAG_RENDERER_TEMPLATE = GWT.create(Template.class);
    
    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:18px;height:12px;background-image:url({0})'></div>")
        SafeHtml image(String imageUri);

        @SafeHtmlTemplates.Template("<div style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:{1}px;height:{2}px;background-image:url({0})'></div>")
        SafeHtml image(String imageUri, int width, int height);

        @SafeHtmlTemplates.Template("<div title='{1}' style='vertical-align:middle;background-repeat:no-repeat;background-size:contain;display:inline-block;width:18px;height:12px;background-image:url({0})'></div>")
        SafeHtml imageWithTitle(String imageUri, String title);
    }
    
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
