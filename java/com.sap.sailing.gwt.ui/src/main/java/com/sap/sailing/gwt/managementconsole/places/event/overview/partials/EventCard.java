package com.sap.sailing.gwt.managementconsole.places.event.overview.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewResources;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewView.Presenter;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.media.ImageDTO;

public class EventCard extends Composite {

    interface EventCardUiBinder extends UiBinder<Widget, EventCard> {
    }

    private static EventCardUiBinder uiBinder = GWT.create(EventCardUiBinder.class);

    @UiField
    EventOverviewResources local_res;

    @UiField
    Element card, title, subTitle;

    @UiField
    Anchor advancedSettingsEventAnchor;

    private final EventDTO event;
    private final Presenter presenter;

    public EventCard(final EventDTO event, final Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
        this.event = event;
        this.presenter = presenter;
        local_res.style().ensureInjected();

        final String title = event.getName();
        String venue = "-";
        if (event.venue != null) {
            venue = event.venue.getName();
        }
        String time = "-";
        if (event.startDate != null && event.endDate != null) {
            time = DateAndTimeFormatterUtil.formatDateRange(event.startDate, event.endDate);
        } else if (event.startDate != null) {
            time = DateAndTimeFormatterUtil.formatDateAndTime(event.startDate);
        }
        String imageUrl = null;
        final ImageDTO teaserImage = event.getTeaserImage();

        if (teaserImage != null) {
            imageUrl = teaserImage.getSourceRef();
        }

        this.title.setInnerSafeHtml(SafeHtmlUtils.fromString(title));
        this.subTitle.setInnerSafeHtml(SafeHtmlUtils.fromString(venue + ", " + time));

        if (imageUrl != null) {
            this.card.getStyle().setBackgroundImage("url(' " + imageUrl + "')");
            this.card.addClassName(local_res.style().customTeaser());
        }
    }

    @UiHandler("advancedSettingsEventAnchor")
    void onSettingsIconClick(final ClickEvent event) {
        presenter.navigateToEvent(this.event);
    }

}
