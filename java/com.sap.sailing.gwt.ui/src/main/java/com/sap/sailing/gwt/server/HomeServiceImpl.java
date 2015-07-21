package com.sap.sailing.gwt.server;

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
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.server.Activator;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO.EventSeriesState;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;

/**
 * The server side implementation of the RPC service.
 */
public class HomeServiceImpl extends ProxiedRemoteServiceServlet implements HomeService {
    private static final long serialVersionUID = 3947782997746039939L;
    
    private static final int MAX_STAGE_EVENTS = 5;
    private static final int MAX_RECENT_EVENTS = 3;
    private static final int MAX_VIDEO_COUNT = 3;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public HomeServiceImpl() {
        BundleContext context = Activator.getDefault();
        
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); 
    }
    
    @Override
    public StartViewDTO getStartView() throws MalformedURLException {
        EventStageCandidateCalculator stageCandidateCalculator = new EventStageCandidateCalculator();
        RecentEventsCalculator recentEventsCalculator = new RecentEventsCalculator();
        
        HomeServiceUtil.forAllPublicEvents(getService(), getThreadLocalRequest(), stageCandidateCalculator, recentEventsCalculator);
        
        StartViewDTO result = new StartViewDTO();
        
        int count = 0;
        for(Pair<StageEventType, EventHolder> pair : stageCandidateCalculator.getFeaturedEvents()) {
            count++;
            if(count > MAX_STAGE_EVENTS) {
                break;
            }
            
            StageEventType stageType = pair.getA();
            EventHolder holder = pair.getB();
            result.addStageEvent(HomeServiceUtil.convertToEventStageDTO(holder.event, holder.baseURL, holder.onRemoteServer, stageType, getService()));
            
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);

            Iterable<VideoDescriptor> videosOfEvent = holder.event.getVideos();
            if (!Util.isEmpty(videosOfEvent) && result.getVideos().size() < MAX_VIDEO_COUNT) {
                VideoDescriptor youTubeRandomUrl = HomeServiceUtil.getRandomVideo(videosOfEvent);

                MimeType type = youTubeRandomUrl.getMimeType();
                if (MediaConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                    SailingVideoDTO candidate = new SailingVideoDTO(eventRef, youTubeRandomUrl.getURL().toString(), type, //
                            youTubeRandomUrl.getCreatedAtDate().asDate()
                            );
                    candidate.setTitle(holder.event.getName());
                    result.addVideo(candidate);
                }
            }
        }
        
        final Set<SailingImageDTO> photoGalleryUrls = new HashSet<>(); // using a HashSet here leads to a reasonable
                                                                        // amount of shuffling
        final List<SailingVideoDTO> videoCandidates = new ArrayList<>();
        
        for(EventHolder holder : recentEventsCalculator.getRecentEventsOfLast12Month()) {
            if(result.getRecentEvents().size() < MAX_RECENT_EVENTS) {
                result.addRecentEvent(HomeServiceUtil.convertToEventListDTO(holder.event, holder.baseURL, holder.onRemoteServer, getService()));
            }
            
            EventBase event = holder.event;
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);

            for (ImageDescriptor url : HomeServiceUtil.getSailingLovesPhotographyImages(event)) {
                if(url.hasSize()) {
                    SailingImageDTO sailingImageDTO = new SailingImageDTO(eventRef, url.getURL().toString(), null);
                    sailingImageDTO.setSizeInPx(url.getWidthInPx(), url.getHeightInPx());
                    photoGalleryUrls.add(sailingImageDTO);
                }
            }
            for (VideoDescriptor videoUrl : event.getVideos()) {
                MimeType type = videoUrl.getMimeType();
                if (MediaConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                    SailingVideoDTO candidate = new SailingVideoDTO(eventRef, videoUrl.getURL().toString(), type, //
                            videoUrl.getCreatedAtDate().asDate()
                    );
                    candidate.setTitle(holder.event.getName());
                    videoCandidates.add(candidate);
                }
            }
        }
        
        final int numberOfCandidatesAvailable = videoCandidates.size();
        if (numberOfCandidatesAvailable <= (MAX_VIDEO_COUNT - result.getVideos().size())) {
            // add all we have, no randomize
            for (SailingVideoDTO video : videoCandidates) {
                result.addVideo(video);
            }
        } else {
            // fill up the list randomly from videoCandidates
            final Random videosRandomizer = new Random(numberOfCandidatesAvailable);
            randomlyPick: for (int i = 0; i < numberOfCandidatesAvailable; i++) {
                int nextVideoindex = videosRandomizer.nextInt(numberOfCandidatesAvailable);
                final SailingVideoDTO video = videoCandidates.get(nextVideoindex);
                result.addVideo(video);
                if (result.getVideos().size() == MAX_VIDEO_COUNT) {
                    break randomlyPick;
                }
            }
        }
        Random random = new Random();
        List<SailingImageDTO> shuffledPhotoGallery = new ArrayList<>(photoGalleryUrls);
        final int gallerySize = photoGalleryUrls.size();
        for (int i = 0; i < gallerySize; i++) {
            Collections.swap(shuffledPhotoGallery, i, random.nextInt(gallerySize));
        }
        for (SailingImageDTO holder : shuffledPhotoGallery) {
            result.addPhoto(holder);
        }
        // TODO media
        return result;
    }
    
    @Override
    public EventSeriesViewDTO getEventSeriesViewById(UUID id) {
        Event o = getService().getEvent(id);
        if (o == null) {
            throw new RuntimeException("Series not found");
        }
        
        EventSeriesViewDTO dto = new EventSeriesViewDTO();
        dto.setId(id);
        ImageDescriptor logoImage = o.findImageWithTag(MediaTagConstants.LOGO);
        dto.setLogoImage(logoImage != null ? HomeServiceUtil.convertToImageDTO(logoImage) : null);
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
                EventMetadataDTO eventOfSeries = HomeServiceUtil.convertToMetadataDTO(eventInSeries, getService());
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
        MediaDTO media = new MediaDTO();
        for(ImageDescriptor image : HomeServiceUtil.getPhotoGalleryImages(event)) {
            SailingImageDTO imageDTO = new SailingImageDTO(eventRef, image.getURL().toString(), image.getCreatedAtDate().asDate());
            imageDTO.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
            imageDTO.setTitle(image.getTitle() != null ? image.getTitle(): eventName);
            imageDTO.setSubtitle(image.getSubtitle());
            imageDTO.setTags(image.getTags());
            imageDTO.setCopyright(image.getCopyright());
            imageDTO.setLocale(image.getLocale() != null ? image.getLocale().toString() : null);
            media.addPhoto(imageDTO);
        }
        for(VideoDescriptor video : event.getVideos()) {
            MimeType type = video.getMimeType();
            if (MediaConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                SailingVideoDTO videoDTO = HomeServiceUtil.toSailingVideoDTO(eventRef, video);
                media.addVideo(videoDTO);
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
        HomeServiceUtil.forAllPublicEvents(getService(), getThreadLocalRequest(), new EventVisitor() {
            @Override
            public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
                EventListEventDTO eventDTO = HomeServiceUtil.convertToEventListDTO(event, baseURL, onRemoteServer, getService());
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
}
