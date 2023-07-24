package com.sap.sailing.gwt.home.desktop.partials.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface SubscriptionCardResources extends SharedHomeResources {

    public static final SubscriptionCardResources INSTANCE = GWT.create(SubscriptionCardResources.class);

    @Source("SubscriptionCard.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String content();
        String free();
        String feature();
        String highlight();
        String owned();
        String subscription();
        String title();
        String description();
        String info();
        String price();
        String priceDisabled();
        String priceInfo();
        String selected();
        String prices();
        String features();
        String subscribe();
        String container();
        String highlightHeader();
        String subscriptionBody();
        String subscriptionFooter();
        String popupGlass();
        String popupContent();
        String subscriptionHeader();
        String hint();
        String featureList();
        String featureTitle();
        String featureDescription();
        String featureLink();
        String featureCheck();
        String featureNone();
        String featureHeader();
        String buttonWarning();
        String confirmationDialog();
    }
}
