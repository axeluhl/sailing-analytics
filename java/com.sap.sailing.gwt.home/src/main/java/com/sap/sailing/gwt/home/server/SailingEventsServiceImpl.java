package com.sap.sailing.gwt.home.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.gwt.home.client.SailingEventsService;
import com.sap.sailing.gwt.home.shared.dto.CourseAreaDTO;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;
import com.sap.sailing.gwt.home.shared.dto.RegattaDTO;
import com.sap.sailing.gwt.home.shared.dto.VenueDTO;
import com.sap.sailing.server.RacingEventService;

@SuppressWarnings("serial")
public class SailingEventsServiceImpl extends ProxiedRemoteServiceServlet implements SailingEventsService {

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private final BundleContext context;

    public SailingEventsServiceImpl() {
        context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
    }

    @Override
    public List<EventDTO> getEvents() {
        List<EventDTO> result = new ArrayList<EventDTO>();
        for (Event event : getRacingEventService().getAllEvents()) {
            EventDTO eventDTO = convertToEventDTO(event);
            result.add(eventDTO);
        }
        return result;
    }

    @Override
    public EventDTO getEventById(String id) {
        EventDTO result = null;
        UUID eventUUID = UUID.fromString(id);
        Event event = getRacingEventService().getEvent(eventUUID);
        if (event != null) {
            result = convertToEventDTO(event);
        }
        return result;
    }

    private EventDTO convertToEventDTO(Event event) {
        EventDTO eventDTO = new EventDTO(event.getName());
        eventDTO.venue = new VenueDTO();
        eventDTO.venue.setName(event.getVenue() != null ? event.getVenue().getName() : null);
        eventDTO.startDate = event.getStartDate() != null ? event.getStartDate().asDate() : null;
        eventDTO.endDate = event.getStartDate() != null ? event.getEndDate().asDate() : null;
        eventDTO.isPublic = event.isPublic();
        eventDTO.uuid = event.getId().toString();
        eventDTO.regattas = new ArrayList<RegattaDTO>();
        for (Regatta regatta : event.getRegattas()) {
            RegattaDTO regattaDTO = new RegattaDTO();
            regattaDTO.setName(regatta.getName());
            eventDTO.regattas.add(regattaDTO);
        }
        eventDTO.venue.setCourseAreas(new ArrayList<CourseAreaDTO>());
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            CourseAreaDTO courseAreaDTO = convertToCourseAreaDTO(courseArea);
            eventDTO.venue.getCourseAreas().add(courseAreaDTO);
        }
        return eventDTO;
    }

    private CourseAreaDTO convertToCourseAreaDTO(CourseArea courseArea) {
        CourseAreaDTO courseAreaDTO = new CourseAreaDTO(courseArea.getName());
        courseAreaDTO.uuid = courseArea.getId().toString();
        return courseAreaDTO;
    }

    protected RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService(); // grab the service
    }

    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }
}
