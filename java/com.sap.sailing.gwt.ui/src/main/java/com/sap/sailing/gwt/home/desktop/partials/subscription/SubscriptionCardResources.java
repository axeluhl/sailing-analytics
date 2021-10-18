package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface SubscriptionCardResources extends SharedHomeResources {

    public static final SubscriptionCardResources INSTANCE = GWT.create(SubscriptionCardResources.class);

    @Source("SubscriptionCard.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String free();
        String feature();
        String highlight();
        String owned();
        String individual();
        String subscription();
        String title();
        String description();
        String price();
        String selected();
        String prices();
        String features();
        String subscribe();
        String container();
        String highlightHeader();
        String subscriptionBody();
        String subscriptionFooter();
    }
}
