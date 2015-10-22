package com.sap.sailing.dashboards.gwt.client.eventlogo;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.media.ImageDTO;

public class EventLogo extends Image {

    private static final Logger logger = Logger.getLogger(EventLogo.class.getName());
    
    protected EventLogo() {
        super();
        this.getElement().addClassName("eventLogo");
    }

    public static EventLogo getEventLogoFromEventId(SailingServiceAsync sailingServiceAsync, String eventId) {
        final EventLogo eventLogo = new EventLogo();
        try {
            final UUID eventUUID = UUID.fromString(eventId);
            logger.log(Level.INFO, "Loading EventDTO for id " + eventId);
            sailingServiceAsync.getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(final EventDTO event) {
                    logger.log(Level.INFO, "Received EventDTO");
                    if (event != null) {
                        final ImageDTO logo = event.getLogoImage();
                        if (logo != null) {
                            eventLogo.getElement().setAttribute("src", logo.getSourceRef());
                        } else {
                            eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                        }
                        if (event.getName() != null) {
                            Window.setTitle(event.getName() + " Dashboard");
                        }
                    } else {
                        logger.log(Level.INFO, "Received EventDTO is null");
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    logger.log(Level.INFO, "Failed to received EventDTO");
                    logger.log(Level.INFO, caught.getMessage());
                    eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                }
            });
        } catch (IllegalArgumentException e) {
            eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
        return eventLogo;
    }
}
