package com.sap.sailing.gwt.managementconsole.places.event.overview;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.places.event.overview.partials.EventCard;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.gwt.client.media.ImageDTO;

public class EventOverviewView extends Composite {
    
    Logger logger = Logger.getLogger(this.getClass().getName());

    interface EventOverviewViewUiBinder extends UiBinder<Widget, EventOverviewView> {
    }

    private static EventOverviewViewUiBinder uiBinder = GWT.create(EventOverviewViewUiBinder.class);

    @UiField
    EventOverviewResources local_res;

    @UiField
    FlowPanel cards;

    public EventOverviewView() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }

    public void renderEvents(List<EventDTO> events) {
        cards.clear();
        for (EventDTO event : events) {
            String title = event.getName();
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
            boolean feature = event.isRunning();
            String imageUrl = null;
            if (event.getImages() != null) {
                Optional<String> imageUrlOptional = event.getImages()
                        .stream()
                        .filter(image -> image.hasTag(MediaTagConstants.TEASER.getName()))
                        .map(image -> image.getSourceRef())
                        .findFirst();
                if (imageUrlOptional.isPresent()) {
                    imageUrl = imageUrlOptional.get();
                }
            } 
            EventCard card = new EventCard(title, venue + ", " + time, feature, imageUrl);
            cards.add(card);
        }
    }

}
