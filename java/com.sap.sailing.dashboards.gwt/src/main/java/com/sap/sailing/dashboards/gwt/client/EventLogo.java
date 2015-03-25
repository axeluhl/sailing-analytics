package com.sap.sailing.dashboards.gwt.client;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventLogo extends Image {

    private static final Logger logger = Logger.getLogger(RibDashboardPanel.class.getName());

    protected EventLogo() {
        super();
        this.getElement().addClassName("eventLogo");
    }

    public static EventLogo getEventLogoFromEventId(SailingServiceAsync sailingServiceAsync, String eventId) {
        final EventLogo eventLogo = new EventLogo();
        eventLogo.getElement().setAttribute("src", "someURL");
        try {
            final UUID eventUUID = UUID.fromString(eventId);
            sailingServiceAsync.getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(final EventDTO event) {
                    logger.log(Level.INFO, "GOT EVENT: ");
                    final String logoUrl = event.getLogoImageURL();
//                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//
//                        @Override
//                        public void execute() {
                            if (logoUrl != null) {
                                eventLogo.getElement().setAttribute("src", logoUrl);
                            } else {
                                eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                            }
//                        }
//                    });
                }

                @Override
                public void onFailure(Throwable caught) {
                    eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                }
            });
        } catch (IllegalArgumentException e) {
            eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        }
        return eventLogo;
    }
}
