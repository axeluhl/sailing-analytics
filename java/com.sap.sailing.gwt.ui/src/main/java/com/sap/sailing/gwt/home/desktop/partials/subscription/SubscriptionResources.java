package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface SubscriptionResources extends SharedHomeResources {

    public static final SubscriptionResources INSTANCE = GWT.create(SubscriptionResources.class);

    @Source("Subscription.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String feature();
        String highlight();
        String subscription();
        String title();
        String description();
        String price();
        String features();
        String subscribe();
        String container();
    }
}
