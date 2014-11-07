package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface SailingAnalyticsResources extends ClientBundle {
    public static final SailingAnalyticsResources INSTANCE = GWT.create(SailingAnalyticsResources.class);

    @Source("release_notes_analyze.html")
    public TextResource getReleaseNotesHtml();

    @Source("features_analyze.html")
    public TextResource getFeaturesHtml();
}
