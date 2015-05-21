package com.sap.sailing.gwt.home.client.place.event.partials.video;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewVideoStageDTO;

public class Video extends Composite {

    private static LivestreamUiBinder uiBinder = GWT.create(LivestreamUiBinder.class);

    interface LivestreamUiBinder extends UiBinder<Widget, Video> {
    }

    public Video() {
        VideoResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(EventOverviewVideoStageDTO data) {
        // TODO
    }
}
