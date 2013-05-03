package com.sap.sailing.gwt.ui.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrack.MimeType;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackBatchOperation;
import com.sap.sailing.server.operationaltransformation.AddMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.RemoveMediaTrackOperation;
import com.sap.sailing.server.operationaltransformation.UpdateMediaTrackOperation;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

//    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private static final long serialVersionUID = -8917349579281305977L;
    
    CountDownLatch initializationProgress = new CountDownLatch(1);

    public MediaServiceImpl() {
        super();
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
        startInitializationThread();
    }

    private MediaDB mediaDB() {
        return MediaDBFactory.INSTANCE.getDefaultMediaDB();
    }

    private RacingEventService racingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    private void waitForInitialization() {
        try {
            int timeout = 10;
            TimeUnit timeoutUnit = TimeUnit.SECONDS;
            if (!initializationProgress.await(timeout, timeoutUnit)) {
                throw new RuntimeException("Media library initialization timed out: " + timeout + ' ' + timeoutUnit);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void startInitializationThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateMediaLibary();
                initializationProgress.countDown();
            }

        }).start();
    }

    /**
     * Collects media track references from the configured sources (mongo DB by default, ftp folder yet to be implemented).
     * The method is expected to be called initially blocking the API until finished.
     * 
     * Subsequent calls (assumed to be triggered from the admin console or in scheduled intervals) don't need to block. In that case,
     * the API will simply serve the current state.
     * 
     */
    private void updateMediaLibary() {
        Collection<DBMediaTrack> allDBMediaTracks = mediaDB().loadAllMediaTracks();
        Collection<MediaTrack> allMediaTracks = new ArrayList<MediaTrack>(allDBMediaTracks.size());
        for (DBMediaTrack dbMediaTrack : allDBMediaTracks) {
            MediaTrack mediaTrack = createMediaTrackFromDB(dbMediaTrack);
            allMediaTracks.add(mediaTrack );
        }
        racingEventService().apply(new AddMediaTrackBatchOperation(allMediaTracks));
    }
    
    private MediaTrack createMediaTrackFromDB(DBMediaTrack dbMediaTrack) {
        MimeType mimeType = dbMediaTrack.mimeType != null ? MimeType.valueOf(dbMediaTrack.mimeType) : null;
        MediaTrack mediaTrack = new MediaTrack(dbMediaTrack.dbId, dbMediaTrack.title, dbMediaTrack.url, dbMediaTrack.startTime, dbMediaTrack.durationInMillis, mimeType);
        return mediaTrack;
    }
    
    @Override
    public Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        waitForInitialization();
        return racingEventService().getMediaTracksForRace(regattaAndRaceIdentifier);
    }

    @Override
    public Collection<MediaTrack> getAllMediaTracks() {
        waitForInitialization();
        return racingEventService().getAllMediaTracks();
    }

    @Override
    public void addMediaTrack(MediaTrack mediaTrack) {
        if (mediaTrack.dbId != null) {
            throw new IllegalStateException("Property dbId must be null for newly created media track.");
        }
        waitForInitialization();
        racingEventService().apply(new AddMediaTrackOperation(mediaTrack));
        String mimeType = mediaTrack.mimeType != null ? mediaTrack.mimeType.name() : null;
        mediaDB().insertMediaTrack(mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.durationInMillis, mimeType);
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        waitForInitialization();
        racingEventService().apply(new RemoveMediaTrackOperation(mediaTrack));
        mediaDB().deleteMediaTrack(mediaTrack.dbId);
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        waitForInitialization();
        racingEventService().apply(new UpdateMediaTrackOperation(mediaTrack));
        mediaDB().updateTitle(mediaTrack.dbId, mediaTrack.title);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        waitForInitialization();
        racingEventService().apply(new UpdateMediaTrackOperation(mediaTrack));
        mediaDB().updateUrl(mediaTrack.dbId, mediaTrack.url);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        waitForInitialization();
        racingEventService().apply(new UpdateMediaTrackOperation(mediaTrack));
        mediaDB().updateStartTime(mediaTrack.dbId, mediaTrack.startTime);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        waitForInitialization();
        racingEventService().apply(new UpdateMediaTrackOperation(mediaTrack));
        mediaDB().updateDuration(mediaTrack.dbId, mediaTrack.durationInMillis);
    }
}
