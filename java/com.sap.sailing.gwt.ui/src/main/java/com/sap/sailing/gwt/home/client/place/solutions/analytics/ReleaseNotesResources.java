package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface ReleaseNotesResources extends ClientBundle {
    public static final ReleaseNotesResources INSTANCE = GWT.create(ReleaseNotesResources.class);

    @Source("release_notes_analyze.html")
    public TextResource getReleaseNotesHtml();

}
