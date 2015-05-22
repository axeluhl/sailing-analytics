package com.sap.sailing.gwt.home.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.common.ImageSize;
import com.sap.sailing.domain.common.media.MediaType;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaUtils;
import com.sap.sailing.gwt.ui.shared.media.VideoMetadataDTO;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * The server side implementation of the RPC service.
 */
public class HomeServiceImpl extends ProxiedRemoteServiceServlet implements HomeService {
    private interface EventVisitor {
        void visit(EventBase event, boolean onRemoteServer, URL baseURL);
    }
    private static class EventHolder {
        EventBase event;
        boolean onRemoteServer;
        URL baseURL;
        public EventHolder(EventBase event, boolean onRemoteServer, URL baseURL) {
            super();
            this.event = event;
            this.onRemoteServer = onRemoteServer;
            this.baseURL = baseURL;
        }
    }
    
    private class FeaturedEventsComparator implements Comparator<Pair<StageEventType, EventHolder>> {
        @Override
        public int compare(Pair<StageEventType, EventHolder> eventAndStageType1,
                Pair<StageEventType, EventHolder> eventAndStageType2) {
            TimePoint now = MillisecondsTimePoint.now();
            TimeRange event1Range = new TimeRangeImpl(eventAndStageType1.getB().event.getStartDate(), eventAndStageType1.getB().event.getEndDate());
            TimeRange event2Range = new TimeRangeImpl(eventAndStageType2.getB().event.getStartDate(), eventAndStageType2.getB().event.getEndDate());
            return event1Range.timeDifference(now).compareTo(event2Range.timeDifference(now));
        }
    }
    
    private static final long serialVersionUID = 3947782997746039939L;
    private static final Logger logger = Logger.getLogger(HomeServiceImpl.class.getName());
    
    private static final int MAX_STAGE_EVENTS = 5;
    private static final int MAX_RECENT_EVENTS = 3;
    private static final int MAX_VIDEO_COUNT = 3;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public HomeServiceImpl() {
        BundleContext context = Activator.getDefault();
        
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); // grab the service
    }
    
    public void forAllPublicEvents(EventVisitor visitor) throws MalformedURLException {
        URL requestedBaseURL = getRequestBaseURL();
        for (Event event : getService().getAllEvents()) {
            if(event.isPublic()) {
                visitor.visit(event, false, requestedBaseURL);
            }
        }
        for (Entry<RemoteSailingServerReference, com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception>> serverRefAndEventsOrException :
                        getService().getPublicEventsOfAllSailingServers().entrySet()) {
            final com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception> eventsOrException = serverRefAndEventsOrException.getValue();
            final RemoteSailingServerReference serverRef = serverRefAndEventsOrException.getKey();
            final Iterable<EventBase> remoteEvents = eventsOrException.getA();
            URL baseURL = getBaseURL(serverRef.getURL());
            if (remoteEvents != null) {
                for (EventBase remoteEvent : remoteEvents) {
                    if(remoteEvent.isPublic()) {
                        visitor.visit(remoteEvent, true, baseURL);
                    }
                }
            }
        }
    }
    
    /**
     * Determines the base URL (protocol, host and port parts) used for the currently executing servlet request. Defaults
     * to <code>http://sapsailing.com</code>.
     * @throws MalformedURLException 
     */
    private URL getRequestBaseURL() throws MalformedURLException {
        final URL url = new URL(getThreadLocalRequest().getRequestURL().toString());
        final URL baseURL = getBaseURL(url);
        return baseURL;
    }

    private URL getBaseURL(URL url) throws MalformedURLException {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), /* file */ "");
    }
    
    @Override
    public StartViewDTO getStartView() throws MalformedURLException {
        final List<Pair<StageEventType, EventHolder>> featuredEvents = new ArrayList<Pair<StageEventType, EventHolder>>();
        final List<EventHolder> recentEventsOfLast12Month = new ArrayList<EventHolder>();
        final TimePoint now = MillisecondsTimePoint.now();
        
        forAllPublicEvents(new EventVisitor() {
            @Override
            public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
                EventHolder holder = new EventHolder(event, onRemoteServer, baseURL);
                if (now.after(event.getStartDate()) && now.before(event.getEndDate())) {
                    featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.RUNNING, holder));
                } else if (event.getStartDate().after(now) &&
                        event.getStartDate().before(now.plus(Duration.ONE_WEEK.times(4)))) {
                    featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.UPCOMING_SOON, holder));
                } else if (event.getEndDate().before(now) &&
                        event.getEndDate().after(now.minus(Duration.ONE_YEAR))) {
                    recentEventsOfLast12Month.add(holder);
                    featuredEvents.add(new Pair<StageEventType, EventHolder>(StageEventType.POPULAR, holder));
                }
            }
        });
        
        StartViewDTO result = new StartViewDTO();
        
        Collections.sort(featuredEvents, new FeaturedEventsComparator());
        for(int i = 0; i < MAX_STAGE_EVENTS && i < featuredEvents.size(); i++) {
            Pair<StageEventType, EventHolder> pair = featuredEvents.get(i);
            StageEventType stageType = pair.getA();
            EventHolder holder = pair.getB();
            result.addStageEvent(convertToEventStageDTO(holder.event, holder.baseURL, holder.onRemoteServer, stageType));
            
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);

            Iterable<URL> videosOfEvent = holder.event.getVideoURLs();
            if (!Util.isEmpty(videosOfEvent) && result.getVideos().size() < MAX_VIDEO_COUNT) {
                URL youTubeRandomUrl = HomeServiceUtil.getRandomURL(videosOfEvent);

                MimeType type = MediaUtils.detectMimeTypeFromUrl(youTubeRandomUrl.toString());
                if (type.mediaType == MediaType.video) {
                    VideoMetadataDTO candidate = new VideoMetadataDTO(eventRef, youTubeRandomUrl, type,
                            holder.event.getName());
                    result.addVideo(candidate);
                }
            }
        }
        Collections.sort(recentEventsOfLast12Month, new Comparator<EventHolder>() {
            @Override
            public int compare(EventHolder o1, EventHolder o2) {
                final long diff = o2.event.getEndDate().asMillis() - o1.event.getEndDate().asMillis();
                return diff > 0l ? 1 : diff < 0l ? -1 : 0;
            }
        });
        
        final Set<ImageMetadataDTO> photoGalleryUrls = new HashSet<>(); // using a HashSet here leads to a reasonable
                                                                        // amount of shuffling
        final List<VideoMetadataDTO> videoCandidates = new ArrayList<>();
        
        for(EventHolder holder : recentEventsOfLast12Month) {
            if(result.getRecentEvents().size() < MAX_RECENT_EVENTS) {
                result.addRecentEvent(convertToEventListDTO(holder.event, holder.baseURL, holder.onRemoteServer));
            }
            
            EventBase event = holder.event;
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);

            for (URL url : HomeServiceUtil.getSailingLovesPhotographyImages(event)) {
                try {
                    ImageSize imageSize = event.getImageSize(url);
                    if(imageSize != null) {
                        // TODO: do we habe a title?
                        photoGalleryUrls.add(new ImageMetadataDTO(eventRef, url, imageSize, null));
                    }
                } catch (Exception e) {
                }
            }
            for (URL videoUrl : event.getVideoURLs()) {
                
                MimeType type = MediaUtils.detectMimeTypeFromUrl(videoUrl.toString());
                if (type.mediaType == MediaType.video) {
                    VideoMetadataDTO candidate = new VideoMetadataDTO(eventRef, videoUrl, type, holder.event.getName());
                    videoCandidates.add(candidate);
                }
            }
        }
        
        final int numberOfCandidatesAvailable = videoCandidates.size();
        if (numberOfCandidatesAvailable <= (MAX_VIDEO_COUNT - result.getVideos().size())) {
            // add all we have, no randomize
            for (VideoMetadataDTO video : videoCandidates) {
                result.addVideo(video);
            }
        } else {
            // fill up the list randomly from videoCandidates
            final Random videosRandomizer = new Random(numberOfCandidatesAvailable);
            randomlyPick: for (int i = 0; i < numberOfCandidatesAvailable; i++) {
                int nextVideoindex = videosRandomizer.nextInt(numberOfCandidatesAvailable);
                final VideoMetadataDTO video = videoCandidates.get(nextVideoindex);
                result.addVideo(video);
                if (result.getVideos().size() == MAX_VIDEO_COUNT) {
                    break randomlyPick;
                }
            }
        }
        Random random = new Random();
        List<ImageMetadataDTO> shuffledPhotoGallery = new ArrayList<>(photoGalleryUrls);
        final int gallerySize = photoGalleryUrls.size();
        for (int i = 0; i < gallerySize; i++) {
            Collections.swap(shuffledPhotoGallery, i, random.nextInt(gallerySize));
        }
        for (ImageMetadataDTO holder : shuffledPhotoGallery) {
            result.addPhoto(holder);
        }
        // TODO media
        return result;
    }

    @Override
    public EventViewDTO getEventViewById(UUID id) {
        Event event = getService().getEvent(id);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }

        EventViewDTO dto = new EventViewDTO();
        mapToMetadataDTO(event, dto);
        
        dto.setLogoImageURL(event.getLogoImageURL() == null ? null : event.getLogoImageURL().toString());
        dto.setOfficialWebsiteURL(event.getOfficialWebsiteURL() == null ? null : event.getOfficialWebsiteURL().toString());

        dto.setHasMedia(HomeServiceUtil.hasMedia(event));
        dto.setState(HomeServiceUtil.calculateEventState(event));
        dto.setHasAnalytics(EventState.RUNNING.compareTo(dto.getState()) <= 0);

        boolean isFakeSeries = HomeServiceUtil.isFakeSeries(event);
        
        for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
            for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                if(leaderboard instanceof RegattaLeaderboard) {
                    Regatta regatta = getService().getRegattaByName(leaderboard.getName());
                    if(isFakeSeries && !HomeServiceUtil.isPartOfEvent(event, regatta)) {
                        continue;
                    }
                    
                    RegattaMetadataDTO regattaDTO = createRegattaMetadataDTO(leaderboardGroup, leaderboard);
                    regattaDTO.setStartDate(regatta.getStartDate() != null ? regatta.getStartDate().asDate() : null);
                    regattaDTO.setEndDate(regatta.getEndDate() != null ? regatta.getEndDate().asDate() : null);
                    regattaDTO.setState(HomeServiceUtil.calculateRegattaState(regattaDTO));
                    dto.getRegattas().add(regattaDTO);
                    
                } else if(leaderboard instanceof FlexibleLeaderboard) {
                    RegattaMetadataDTO regattaDTO = createRegattaMetadataDTO(leaderboardGroup, leaderboard);
                    
                    regattaDTO.setStartDate(null);
                    regattaDTO.setEndDate(null);
                    regattaDTO.setState(HomeServiceUtil.calculateRegattaState(regattaDTO));
                    dto.getRegattas().add(regattaDTO);
                }
            }
        }
        
        if (isFakeSeries) {
            dto.setType(EventType.SERIES_EVENT);
            
            LeaderboardGroup overallLeaderboardGroup = event.getLeaderboardGroups().iterator().next();
            dto.setSeriesName(overallLeaderboardGroup.getDisplayName() != null ? overallLeaderboardGroup.getDisplayName() :overallLeaderboardGroup.getName());
            List<Event> fakeSeriesEvents = new ArrayList<Event>();
            
            for (Event eventOfSeries : getService().getAllEvents()) {
                for (LeaderboardGroup leaderboardGroup : eventOfSeries.getLeaderboardGroups()) {
                    if (overallLeaderboardGroup.equals(leaderboardGroup)) {
                        fakeSeriesEvents.add(eventOfSeries);
                    }
                }
            }
            Collections.sort(fakeSeriesEvents, new Comparator<Event>() {
                public int compare(Event e1, Event e2) {
                    return e1.getStartDate().compareTo(e2.getEndDate());
                }
            });
            for(Event eventInSeries: fakeSeriesEvents) {
                String displayName = getLocation(eventInSeries);
                if(displayName == null) {
                    displayName = eventInSeries.getName();
                }
                dto.getEventsOfSeries().add(new EventReferenceDTO(eventInSeries.getId(), displayName));
            }
        } else {
            dto.setType(dto.getRegattas().size() == 1 ? EventType.SINGLE_REGATTA: EventType.MULTI_REGATTA);
        }

        return dto;
    }

    private RegattaMetadataDTO createRegattaMetadataDTO(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        RegattaMetadataDTO regattaDTO = new RegattaMetadataDTO(leaderboard.getName(), leaderboard.getName());
        regattaDTO.setBoatCategory(leaderboardGroup.getDisplayName() != null ? leaderboardGroup.getDisplayName() : leaderboardGroup.getName());
        regattaDTO.setCompetitorsCount(HomeServiceUtil.calculateCompetitorsCount(leaderboard));
        regattaDTO.setRaceCount(HomeServiceUtil.calculateRaceCount(leaderboard));
        regattaDTO.setTrackedRacesCount(HomeServiceUtil.calculateTrackedRaceCount(leaderboard));
        regattaDTO.setBoatClass(HomeServiceUtil.calculateBoatClass(leaderboard));
        
        return regattaDTO;
    }
    
    @Override
    public EventSeriesViewDTO getEventSeriesViewById(UUID id) {
        Event o = getService().getEvent(id);
        if (o == null) {
            throw new RuntimeException("Series not found");
        }
        
        EventSeriesViewDTO dto = new EventSeriesViewDTO();
        dto.setId(id);
        dto.setLogoImageURL(o.getLogoImageURL() == null ? null : o.getLogoImageURL().toString());
        // TODO implement correctly. We currently do not show media for series.
        dto.setHasMedia(false);
        
        boolean oneEventStarted = false;
        boolean oneEventLive = false;
        boolean allFinished = true;
        if (HomeServiceUtil.isFakeSeries(o)) {
            LeaderboardGroup overallLeaderboardGroup = o.getLeaderboardGroups().iterator().next();
            dto.setDisplayName(overallLeaderboardGroup.getDisplayName() != null ? overallLeaderboardGroup.getDisplayName() : overallLeaderboardGroup.getName());

            if (overallLeaderboardGroup.getOverallLeaderboard() != null) {
                dto.setLeaderboardId(overallLeaderboardGroup.getOverallLeaderboard().getName());
            }

            List<Event> fakeSeriesEvents = new ArrayList<Event>();
            for (Event event : getService().getAllEvents()) {
                for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                    if (overallLeaderboardGroup.equals(leaderboardGroup)) {
                        fakeSeriesEvents.add(event);
                    }
                }
            }
            Collections.sort(fakeSeriesEvents, new Comparator<Event>() {
                public int compare(Event e1, Event e2) {
                    return e1.getStartDate().compareTo(e2.getEndDate());
                }
            });
            for(Event eventInSeries: fakeSeriesEvents) {
                EventMetadataDTO eventOfSeries = convertToMetadataDTO(eventInSeries);
                dto.addEvent(eventOfSeries);
                
                oneEventStarted |= eventOfSeries.isStarted();
                oneEventLive |= (eventOfSeries.isStarted() && !eventOfSeries.isFinished());
                allFinished &= eventOfSeries.isFinished();
            }
        }
        if(oneEventLive) {
            dto.setState(EventSeriesState.RUNNING);
        } else if(!oneEventStarted) {
            dto.setState(EventSeriesState.UPCOMING);
        } else if(allFinished) {
            dto.setState(EventSeriesState.FINISHED);
        } else {
            dto.setState(EventSeriesState.IN_PROGRESS);
        }
        
        dto.setHasAnalytics(oneEventStarted);
        return dto;
    }

    @Override
    public MediaDTO getMediaForEvent(UUID eventId) {
        Event event = getService().getEvent(eventId);
        EventReferenceDTO eventRef = new EventReferenceDTO(event);

        String eventName = event.getName();
        // TODO implement correctly and fill metadata
        MediaDTO media = new MediaDTO();
        for(URL url : HomeServiceUtil.getPhotoGalleryImageURLs(event)) {
            ImageSize imageSize = null;
            try {
                imageSize = event.getImageSize(url);
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.FINE, "Was unable to obtain image size for "+url+" earlier.", e);
            }
            ImageMetadataDTO entry = new ImageMetadataDTO(eventRef, url, imageSize, eventName);
            media.addPhoto(entry);
        }
        for(URL url : event.getVideoURLs()) {
            MimeType type = MediaUtils.detectMimeTypeFromUrl(url.toString());
            if (type.mediaType == MediaType.video) {
                VideoMetadataDTO candidate = new VideoMetadataDTO(eventRef, url, type, null);
                media.addVideo(candidate);
            }
        }
        return media;
    }

    @Override
    public MediaDTO getMediaForEventSeries(UUID seriesId) {
        // TODO implement correctly. We currently do not show media for series.
        return getMediaForEvent(seriesId);
    }
    
    @Override
    public EventListViewDTO getEventListView() throws MalformedURLException {
        // TODO fill stats of years
        final EventListViewDTO result = new EventListViewDTO();
        forAllPublicEvents(new EventVisitor() {
            @Override
            public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
                EventListEventDTO eventDTO = convertToEventListDTO(event, baseURL, onRemoteServer);
                result.addEvent(eventDTO, getYear(eventDTO.getStartDate()));
            }
        });
        return result;
    }

    private int getYear(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    private EventStageDTO convertToEventStageDTO(EventBase event, URL baseURL, boolean onRemoteServer, StageEventType stageType) {
        EventStageDTO dto = new EventStageDTO();
        mapToMetadataDTO(event, dto);
        dto.setBaseURL(baseURL.toString());
        dto.setOnRemoteServer(onRemoteServer);
        dto.setStageType(stageType);
        dto.setStageImageURL(HomeServiceUtil.getStageImageURLAsString(event));
        return dto;
    }
    
    private EventListEventDTO convertToEventListDTO(EventBase event, URL baseURL, boolean onRemoteServer) {
        EventListEventDTO dto = new EventListEventDTO();
        mapToMetadataDTO(event, dto);
        dto.setBaseURL(baseURL.toString());
        dto.setOnRemoteServer(onRemoteServer);
        return dto;
    }
    
    private EventMetadataDTO convertToMetadataDTO(EventBase event) {
        EventMetadataDTO dto = new EventMetadataDTO();
        mapToMetadataDTO(event, dto);
        return dto;
    }
    
    private void mapToMetadataDTO(EventBase event, EventMetadataDTO dto) {
        dto.setId((UUID) event.getId());
        dto.setDisplayName(event.getName());
        dto.setStartDate(event.getStartDate().asDate());
        dto.setEndDate(event.getEndDate().asDate());
        dto.setState(HomeServiceUtil.calculateEventState(event));
        dto.setVenue(event.getVenue().getName());
        if(HomeServiceUtil.isFakeSeries(event)) {
            dto.setLocation(getLocation(event));
        }
        dto.setThumbnailImageURL(HomeServiceUtil.findEventThumbnailImageUrlAsString(event));
    }
    
    public String getLocation(EventBase eventBase) {
        if(!(eventBase instanceof Event)) {
            return null;
        }
        Event event = (Event) eventBase;
        for (Leaderboard leaderboard : event.getLeaderboardGroups().iterator().next().getLeaderboards()) {
            if(leaderboard instanceof RegattaLeaderboard) {
                Regatta regattaEntity = getService().getRegattaByName(leaderboard.getName());
                if(!HomeServiceUtil.isPartOfEvent(event, regattaEntity)) {
                    continue;
                }
            }
            return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName();
        }
        return null;
    }
}
