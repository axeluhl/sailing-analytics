package com.sap.sailing.gwt.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;
import com.sap.sailing.gwt.ui.shared.start.StageEventType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MediaDescriptor;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.VideoDescriptor;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public final class HomeServiceUtil {
    
    private HomeServiceUtil() {
    }

    private static final int MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS = 500;
    
    public static String findEventThumbnailImageUrlAsString(EventBase event) {
        ImageDescriptor url = findEventThumbnailImage(event);
        return url == null ? null : url.getURL().toString();
    }
    
    public static boolean isFakeSeries(EventBase event) {
        Iterator<? extends LeaderboardGroupBase> lgIter = event.getLeaderboardGroups().iterator();
        if(!lgIter.hasNext()) {
            return false;
        }
        LeaderboardGroupBase lg = lgIter.next();
        if(lgIter.hasNext()) {
            return false;
        }
        return lg.hasOverallLeaderboard();
    }
    
    public static boolean isSingleRegatta(Event event) {
        boolean first = true;
        for(LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for(@SuppressWarnings("unused") Leaderboard lb: lg.getLeaderboards()) {
                if(!first) {
                    return false;
                }
                first = false;
            }
        }
        return true;
    }
    
    public static RegattaState calculateRegattaState(RegattaMetadataDTO regatta) {
        Date now = new Date();
        Date startDate = regatta.getStartDate();
        Date endDate = regatta.getEndDate();
        if(startDate != null && now.compareTo(startDate) < 0) {
            return RegattaState.UPCOMING;
        }
        if(endDate != null && now.compareTo(endDate) > 0) {
            return RegattaState.FINISHED;
        }
        if(startDate != null && now.compareTo(startDate) >= 0 && endDate != null && now.compareTo(endDate) <= 0) {
            return RegattaState.RUNNING;
        }
        return RegattaState.UNKNOWN;
    }
    
    public static EventState calculateEventState(EventBase event) {
        return calculateEventState(event.isPublic(), event.getStartDate().asDate(), event.getEndDate().asDate());
    }
    
    public static EventState calculateEventState(boolean isPublic, Date startDate, Date endDate) {
        Date now = new Date();
        if(now.compareTo(startDate) < 0) {
            if(isPublic) {
                return EventState.UPCOMING;
            }
            return EventState.PLANNED;
        }
        if(now.compareTo(endDate) > 0) {
            return EventState.FINISHED;
        }
        return EventState.RUNNING;
    }
    
    public static VideoDTO toVideoDTO(VideoDescriptor video) {
        VideoDTO videoDTO = new VideoDTO(video.getURL().toString(), video.getMimeType(), video.getCreatedAtDate().asDate());
        fillVideoDTOFields(video, videoDTO);
        return videoDTO;
    }
    
    public static SailingVideoDTO toSailingVideoDTO(EventReferenceDTO eventRef, VideoDescriptor video) {
        SailingVideoDTO videoDTO = new SailingVideoDTO(eventRef, video.getURL().toString(), video.getMimeType(), video.getCreatedAtDate().asDate());
        fillVideoDTOFields(video, videoDTO);
        return videoDTO;
    }

    private static void fillVideoDTOFields(VideoDescriptor video, VideoDTO videoDTO) {
        videoDTO.setTitle(video.getTitle());
        videoDTO.setSubtitle(video.getSubtitle());
        videoDTO.setTags(video.getTags());
        videoDTO.setCopyright(video.getCopyright());
        videoDTO.setLocale(video.getLocale() != null ? video.getLocale().toString() : null);
        videoDTO.setLengthInSeconds(video.getLengthInSeconds());
        videoDTO.setThumbnailRef(video.getThumbnailURL() != null ? video.getThumbnailURL().toString(): null);
    }
    
    private static ImageDescriptor findEventThumbnailImage(EventBase event) {
        return event.findImageWithTag(MediaTagConstants.TEASER);
    }
    
    public static String getStageImageURLAsString(final EventBase event) {
        ImageDescriptor image = getStageImage(event);
        return image == null ? null : image.getURL().toString();
    }
    
    public static ImageDescriptor getStageImage(final EventBase event) {
        return event.findImageWithTag(MediaTagConstants.STAGE);
    }

    public static List<String> getPhotoGalleryImageURLsAsString(EventBase event) {
        List<ImageDescriptor> urls = getPhotoGalleryImages(event);
        List<String> result = new ArrayList<String>(urls.size());
        for (ImageDescriptor url : urls) {
            result.add(url.getURL().toString());
        }
        return result;
    }

    public static List<ImageDescriptor> getPhotoGalleryImages(EventBase event) {
        return event.findImagesWithTag(MediaTagConstants.GALLERY);
    }
    
    public static List<ImageDescriptor> getSailingLovesPhotographyImages(EventBase event) {
        final List<ImageDescriptor> acceptedImages = new LinkedList<>();
        for (ImageDescriptor candidateImageUrl : event.getImages()) {
            if (candidateImageUrl.hasSize() && candidateImageUrl.getHeightInPx() > MINIMUM_IMAGE_HEIGHT_FOR_SAILING_PHOTOGRAPHY_IN_PIXELS) {
                acceptedImages.add(candidateImageUrl);
            }
        }
        return acceptedImages;
    }

    public static int calculateCompetitorsCount(Leaderboard sl) {
        return Util.size(sl.getCompetitors());
    }
    
    public static int calculateRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            count += Util.size(column.getFleets());
        }
        return count;
    }
    
    public static int calculateTrackedRaceCount(Leaderboard sl) {
        int count=0;
        for (RaceColumn column : sl.getRaceColumns()) {
            for (Fleet fleet : column.getFleets()) {
                TrackedRace trackedRace = column.getTrackedRace(fleet);
                if(trackedRace != null && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static String getBoatClassName(Leaderboard leaderboard) {
        BoatClass boatClass = getBoatClass(leaderboard);
        return boatClass == null ? null : boatClass.getName();
    }

    private static BoatClass getBoatClass(Leaderboard leaderboard) {
        if(leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            BoatClass boatClassFromRegatta = regattaLeaderboard.getRegatta().getBoatClass();
            if(boatClassFromRegatta != null) {
                return boatClassFromRegatta;
            }
        }
        return getBoatClassFromTrackedRaces(leaderboard);
    }

    private static BoatClass getBoatClassFromTrackedRaces(Leaderboard leaderboard) {
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            return trackedRace.getRace().getBoatClass();
        }
        return null;
    }

    public static boolean hasMedia(Event event) {
        return hasVideos(event) || hasPhotos(event);
    }
    
    public static boolean hasPhotos(Event event) {
        return event.hasImageWithTag(MediaTagConstants.GALLERY);
    }
    
    public static boolean hasVideos(Event event) {
        return !Util.isEmpty(event.getVideos());
    }

    public static boolean isPartOfEvent(Event event, Leaderboard regattaEntity) {
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(courseArea.equals(regattaEntity.getDefaultCourseArea())) {
                return true;
            }
        }
        return false;
    }
    
    public static VideoDescriptor getRandomVideo(Iterable<VideoDescriptor> urls) {
        if(Util.isEmpty(urls)) {
            return null;
        }
        int size = Util.size(urls);
        return Util.get(urls, new Random(size).nextInt(size));
    }
    
    public static VideoDescriptor getStageVideo(Event event, Locale locale, Collection<String> rankedTags, boolean acceptOtherTags) {
        VideoDescriptor bestMatch = null;
        
        for (VideoDescriptor videoCandidate : event.getVideos()) {
            if(!MediaConstants.SUPPORTED_VIDEO_TYPES.contains(videoCandidate.getMimeType())) {
                continue;
            }
            
            if(!acceptOtherTags && !hasOneTag(videoCandidate, rankedTags)) {
                continue;
            }
            
            LocaleMatch localeMatch = matchLocale(videoCandidate, locale);
            if(localeMatch == LocaleMatch.NO_MATCH) {
                continue;
            }
            
            if(bestMatch == null) {
                bestMatch = videoCandidate;
                continue;
            }
            
            int compareByTag = compareByTag(videoCandidate, bestMatch, rankedTags);
            if(compareByTag > 0 || (compareByTag == 0 && isBetter(videoCandidate, bestMatch, locale))) {
                bestMatch = videoCandidate;
                continue;
            }
        }
        return bestMatch;
    }
    
    private static int compareByTag(VideoDescriptor videoCandidate, VideoDescriptor bestMatch,
            Collection<String> rankedTags) {
        for(String rankedTag : rankedTags) {
            boolean hasTag = hasTag(videoCandidate, rankedTag);
            boolean hasTagBestMatch = hasTag(bestMatch, rankedTag);
            if(hasTag != hasTagBestMatch) {
                return hasTag ? 1 : -1;
            }
        }
        return 0;
    }

    private static boolean isBetter(VideoDescriptor candidate, VideoDescriptor reference, Locale locale) {
        LocaleMatch localeMatch = matchLocale(candidate, locale);
        LocaleMatch localeMatchRef = matchLocale(reference, locale);
        if(localeMatch != localeMatchRef) {
            return localeMatch.compareTo(localeMatchRef) < 0 ? true : false;
        }
        
        // TODO filter by length
        
        return candidate.getCreatedAtDate().compareTo(reference.getCreatedAtDate()) > 0;
    }
    
    private static boolean hasTag(MediaDescriptor videoCandidate, String tag) {
        return Util.contains(videoCandidate.getTags(), tag);
    }

    private static boolean hasOneTag(MediaDescriptor videoCandidate, Collection<String> acceptedTags) {
        for(String tag : videoCandidate.getTags()) {
            if(acceptedTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }
    
    private enum LocaleMatch {
        PERFECT, NOT_TAGGED, EN_FALLBACK, NO_MATCH
    }

    private static LocaleMatch matchLocale(VideoDescriptor videoCandidate, Locale locale) {
        Locale localeOfCandidate = videoCandidate.getLocale();
        if(localeOfCandidate == null) {
            return LocaleMatch.NOT_TAGGED;
        }
        if(videoCandidate.getLocale().equals(locale)) {
            return LocaleMatch.PERFECT;
        }
        if(videoCandidate.getLocale().equals(Locale.ENGLISH)) {
            return LocaleMatch.EN_FALLBACK;
        }
        return LocaleMatch.NO_MATCH;
    }
    
    public static EventStageDTO convertToEventStageDTO(EventBase event, URL baseURL, boolean onRemoteServer, StageEventType stageType, RacingEventService service) {
        EventStageDTO dto = new EventStageDTO();
        mapToMetadataDTO(event, dto, service);
        dto.setBaseURL(baseURL.toString());
        dto.setOnRemoteServer(onRemoteServer);
        dto.setStageType(stageType);
        dto.setStageImageURL(HomeServiceUtil.getStageImageURLAsString(event));
        return dto;
    }
    
    public static EventListEventDTO convertToEventListDTO(EventBase event, URL baseURL, boolean onRemoteServer, RacingEventService service) {
        EventListEventDTO dto = new EventListEventDTO();
        mapToMetadataDTO(event, dto, service);
        dto.setBaseURL(baseURL.toString());
        dto.setOnRemoteServer(onRemoteServer);
        return dto;
    }
    
    public static EventMetadataDTO convertToMetadataDTO(EventBase event, RacingEventService service) {
        EventMetadataDTO dto = new EventMetadataDTO();
        mapToMetadataDTO(event, dto, service);
        return dto;
    }
    
    public static void mapToMetadataDTO(EventBase event, EventMetadataDTO dto, RacingEventService service) {
        dto.setId((UUID) event.getId());
        dto.setDisplayName(getEventDisplayName(event, service));
        dto.setStartDate(event.getStartDate().asDate());
        dto.setEndDate(event.getEndDate().asDate());
        dto.setState(HomeServiceUtil.calculateEventState(event));
        dto.setVenue(event.getVenue().getName());
        if(HomeServiceUtil.isFakeSeries(event)) {
            dto.setLocation(getLocation(event, service));
        }
        dto.setThumbnailImageURL(HomeServiceUtil.findEventThumbnailImageUrlAsString(event));
    }
    
    public static String getEventDisplayName(EventBase event, RacingEventService service) {
        if(isFakeSeries(event)) {
            String seriesName = getSeriesName(event);
            if(seriesName != null) {
                String location = getLocation(event, service);
                if(location != null) {
                    return seriesName + " - " + location;
                }
            }
        }
        return event.getName();
    }

    public static String getSeriesName(EventBase event) {
        LeaderboardGroupBase overallLeaderboardGroup = event.getLeaderboardGroups().iterator().next();
        return getLeaderboardDisplayName(overallLeaderboardGroup);
    }

    public static String getLeaderboardDisplayName(LeaderboardGroupBase overallLeaderboardGroup) {
        return overallLeaderboardGroup.getDisplayName() != null ? overallLeaderboardGroup.getDisplayName() : overallLeaderboardGroup.getName();
    }
    
    public static String getLocation(EventBase eventBase, RacingEventService service) {
        if(!(eventBase instanceof Event)) {
            return null;
        }
        Event event = (Event) eventBase;
        for (Leaderboard leaderboard : event.getLeaderboardGroups().iterator().next().getLeaderboards()) {
            if(leaderboard instanceof RegattaLeaderboard) {
                if(!HomeServiceUtil.isPartOfEvent(event, leaderboard)) {
                    continue;
                }
            }
            return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName();
        }
        return null;
    }
    
    public static ImageDTO convertToImageDTO(ImageDescriptor image) {
        ImageDTO result = new ImageDTO(image.getURL().toString(), image.getCreatedAtDate() != null ? image.getCreatedAtDate().asDate() : null);
        result.setCopyright(image.getCopyright());
        result.setTitle(image.getTitle());
        result.setSubtitle(image.getSubtitle());
        result.setMimeType(image.getMimeType());
        result.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
        result.setLocale(image.getLocale() != null ? image.getLocale().toString() : null);
        List<String> tags = new ArrayList<String>();
        for(String tag: image.getTags()) {
            tags.add(tag);
        }
        result.setTags(tags);
        return result;
    }
    
    public static RegattaMetadataDTO toRegattaMetadataDTO(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        RegattaMetadataDTO regattaDTO = new RegattaMetadataDTO(leaderboard.getName(), leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName());
        regattaDTO.setBoatCategory(leaderboardGroup.getDisplayName() != null ? leaderboardGroup.getDisplayName() : leaderboardGroup.getName());
        regattaDTO.setCompetitorsCount(calculateCompetitorsCount(leaderboard));
        regattaDTO.setRaceCount(calculateRaceCount(leaderboard));
        regattaDTO.setTrackedRacesCount(calculateTrackedRaceCount(leaderboard));
        regattaDTO.setBoatClass(getBoatClassName(leaderboard));
        
        return regattaDTO;
    }
    
    public static boolean hasLiveRace(LeaderboardDTO leaderboard) {
        List<Pair<RaceColumnDTO, FleetDTO>> liveRaces = leaderboard.getLiveRaces(getLiveTimePointInMillis());
        return !liveRaces.isEmpty();
    }
    
    private static long getLiveTimePointInMillis() {
        // TODO better solution
        long livePlayDelayInMillis = 15_000;
        return System.currentTimeMillis() - livePlayDelayInMillis;
    }
}
