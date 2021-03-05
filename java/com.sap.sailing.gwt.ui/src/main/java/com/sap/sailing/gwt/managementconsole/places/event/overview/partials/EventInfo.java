package com.sap.sailing.gwt.managementconsole.places.event.overview.partials;

import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.formatDateRange;
import static java.util.Optional.ofNullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Named;

public class EventInfo extends Widget {

    interface EventInfoUiBinder extends UiBinder<Element, EventInfo> {
    }

    private static EventInfoUiBinder uiBinder = GWT.create(EventInfoUiBinder.class);

    @UiField
    EventOverviewResources local_res;

    @UiField
    Element title, details;

    public EventInfo(final EventDTO event) {
        setElement(uiBinder.createAndBindUi(this));

        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(event.getName()));

        final String venue = ofNullable(event.venue).map(Named::getName).orElse("-");
        final String time = formatDateRange(event.startDate, event.endDate);
        this.details.setInnerSafeHtml(SafeHtmlUtils.fromString(venue + ", " + time));
    }

}
