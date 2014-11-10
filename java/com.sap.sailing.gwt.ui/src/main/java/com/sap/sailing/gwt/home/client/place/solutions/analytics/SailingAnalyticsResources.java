package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface SailingAnalyticsResources extends ClientBundle {
    public static final SailingAnalyticsResources INSTANCE = GWT.create(SailingAnalyticsResources.class);

    @Source("com/sap/sailing/gwt/home/solutions/analytics_releasenotes.html")
    public TextResource getReleaseNotesHtml();

    @Source("com/sap/sailing/gwt/home/solutions/analytics_features.html")
    public TextResource getFeaturesHtml();
    
    @Source("com/sap/sailing/gwt/home/solutions/analytics_overview.html")
    public TextResource getOverviewHtml();
}
