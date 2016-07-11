package com.sap.sailing.gwt.ui.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.server.RacingEventService;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

//    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private static final long serialVersionUID = -8917349579281305977L;
    
    public MediaServiceImpl() {
        super();
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
    }

    private RacingEventService racingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    @Override
    public Iterable<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return racingEventService().getMediaTracksForRace(regattaAndRaceIdentifier);
    }
    
    @Override
    public Iterable<MediaTrack> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return racingEventService().getMediaTracksInTimeRange(regattaAndRaceIdentifier);
    }

    @Override
    public Iterable<MediaTrack> getAllMediaTracks() {
        return racingEventService().getAllMediaTracks();
    }

    @Override
    public String addMediaTrack(MediaTrack mediaTrack) {
        if (mediaTrack.dbId != null) {
            throw new IllegalStateException("Property dbId must not be null for newly created media track.");
        }
        racingEventService().mediaTrackAdded(mediaTrack);
        return mediaTrack.dbId;
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        racingEventService().mediaTrackDeleted(mediaTrack);
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        racingEventService().mediaTrackTitleChanged(mediaTrack);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        racingEventService().mediaTrackUrlChanged(mediaTrack);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        racingEventService().mediaTrackStartTimeChanged(mediaTrack);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        racingEventService().mediaTrackDurationChanged(mediaTrack);
    }

    @Override
    public void updateRace(MediaTrack mediaTrack) {
        racingEventService().mediaTrackAssignedRacesChanged(mediaTrack);
        
    }

}
