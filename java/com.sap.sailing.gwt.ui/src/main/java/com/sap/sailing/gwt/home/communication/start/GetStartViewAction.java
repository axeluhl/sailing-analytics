package com.sap.sailing.gwt.home.communication.start;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventLinkDTO;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.server.EventHolder;
import com.sap.sailing.gwt.server.EventStageCandidateCalculator;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.server.RecentEventsCalculator;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.VideoDescriptor;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown on desktop start page (including stage events, recent
 * events and some media contents), preparing the appropriate data structure.
 * </p>
 */
public class GetStartViewAction implements SailingAction<StartViewDTO>, IsClientCacheable {
    private static final int MAX_RECENT_EVENTS = 3;
    private static final int MAX_VIDEO_COUNT = 3;

    /**
     * Creates a new {@link GetStartViewAction} instance.
     */
    public GetStartViewAction() {
    }
    
    @GwtIncompatible
    public StartViewDTO execute(SailingDispatchContext context) {
        EventStageCandidateCalculator stageCandidateCalculator = new EventStageCandidateCalculator();
        RecentEventsCalculator recentEventsCalculator = new RecentEventsCalculator();
        HomeServiceUtil.forAllPublicEventsWithReadPermission(context.getRacingEventService(), context.getRequest(),
                context.getSecurityService(), stageCandidateCalculator, recentEventsCalculator);
        StartViewDTO result = new StartViewDTO();
        int count = 0;
        for (Pair<StageEventType, EventHolder> pair : stageCandidateCalculator.getFeaturedEvents()) {
            StageEventType stageType = pair.getA();
            EventHolder holder = pair.getB();
            count++;
            if (count > EventStageCandidateCalculator.MAX_STAGE_EVENTS && stageType != StageEventType.RUNNING) {
                break;
            }
            result.addStageEvent(HomeServiceUtil.convertToEventStageDTO(holder.event, holder.baseURL, holder.onRemoteServer, stageType, context.getRacingEventService(), false));
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);
            Iterable<VideoDescriptor> videosOfEvent = holder.event.getVideos();
            if (!Util.isEmpty(videosOfEvent) && result.getVideos().size() < MAX_VIDEO_COUNT) {
                VideoDescriptor youTubeRandomUrl = HomeServiceUtil.getRandomVideo(videosOfEvent);
                MimeType type = youTubeRandomUrl.getMimeType();
                if (MediaTagConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                    SailingVideoDTO candidate = new SailingVideoDTO(eventRef, youTubeRandomUrl.getURL().toString(), type,
                            youTubeRandomUrl.getCreatedAtDate().asDate());
                    candidate.setTitle(holder.event.getName());
                    result.addVideo(candidate);
                }
            }
        }
        final Set<SailingImageDTO> photoGalleryUrls = new HashSet<>(); // using a HashSet here leads to a reasonable
                                                                       // amount of shuffling
        final List<SailingVideoDTO> videoCandidates = new ArrayList<>();
        for (EventHolder holder : recentEventsCalculator.getRecentEventsOfLast12Month()) {
            if (result.getRecentEvents().size() < MAX_RECENT_EVENTS) {
                result.addRecentEvent(HomeServiceUtil.convertToEventListDTO(holder.event, holder.baseURL, holder.onRemoteServer, context.getRacingEventService()));
            }
            EventBase event = holder.event;
            EventLinkDTO eventLink = HomeServiceUtil.convertToEventLinkDTO(holder.event, holder.baseURL,
                    holder.onRemoteServer, context.getRacingEventService());
            EventReferenceDTO eventRef = new EventReferenceDTO(holder.event);
            for (ImageDescriptor url : HomeServiceUtil.getSailingLovesPhotographyImages(event)) {
                if (url.hasSize()) {
                    SailingImageDTO sailingImageDTO = new SailingImageDTO(eventLink, url.getURL().toString(), null);
                    sailingImageDTO.setSizeInPx(url.getWidthInPx(), url.getHeightInPx());
                    photoGalleryUrls.add(sailingImageDTO);
                }
            }
            for (VideoDescriptor videoUrl : event.getVideos()) {
                MimeType type = videoUrl.getMimeType();
                if (MediaTagConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
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
    public void cacheInstanceKey(StringBuilder key) {
    }
}
