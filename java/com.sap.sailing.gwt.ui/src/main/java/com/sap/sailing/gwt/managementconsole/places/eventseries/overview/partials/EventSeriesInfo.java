package com.sap.sailing.gwt.managementconsole.places.eventseries.overview.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.places.eventseries.overview.EventSeriesOverviewResources;

public class EventSeriesInfo extends Widget {

    interface EventSeriesInfoUiBinder extends UiBinder<Element, EventSeriesInfo> {
    }

    private static EventSeriesInfoUiBinder uiBinder = GWT.create(EventSeriesInfoUiBinder.class);

    @UiField
    EventSeriesOverviewResources local_res;

    @UiField
    Element title, details;

    public EventSeriesInfo(final EventSeriesMetadataDTO event) {
        setElement(uiBinder.createAndBindUi(this));

        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(event.getSeriesDisplayName()));

        this.details.setInnerSafeHtml(SafeHtmlUtils.fromString("-"));
    }

}
