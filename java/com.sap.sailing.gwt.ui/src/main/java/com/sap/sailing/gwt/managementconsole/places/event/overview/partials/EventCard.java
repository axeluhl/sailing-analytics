package com.sap.sailing.gwt.managementconsole.places.event.overview.partials;

import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.formatDateRange;
import static java.util.Optional.ofNullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewView.Presenter;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Named;
import com.sap.sse.gwt.client.media.ImageDTO;

public class EventCard extends Composite {

    interface EventCardUiBinder extends UiBinder<Widget, EventCard> {
    }

    private static EventCardUiBinder uiBinder = GWT.create(EventCardUiBinder.class);

    @UiField
    EventOverviewResources local_res;
    
    @UiField
    ManagementConsoleResources app_res;

    @UiField
    Element card, container, title, details;

    public EventCard(final EventDTO event, final Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();

        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(event.getName()));

        final String venue = ofNullable(event.venue).map(Named::getName).orElse("-");
        final String time = formatDateRange(event.startDate, event.endDate);
        this.details.setInnerSafeHtml(SafeHtmlUtils.fromString(venue + ", " + time));

        ofNullable(event.getTeaserImage()).map(ImageDTO::getSourceRef).ifPresent(imageUrl -> {
            this.card.getStyle().setBackgroundImage("url(' " + imageUrl + "')");
            this.card.addClassName(local_res.style().customTeaser());
        });

        Event.sinkEvents(card, Event.ONCLICK);
        Event.setEventListener(card, e -> presenter.navigateToEvent(event));
    }

    @UiHandler("contextMenu")
    void onSettingsIconClick(final ClickEvent event) {
        Window.alert("TODO: Open context menu ...");
        event.stopPropagation();
        event.preventDefault();
    }

}
