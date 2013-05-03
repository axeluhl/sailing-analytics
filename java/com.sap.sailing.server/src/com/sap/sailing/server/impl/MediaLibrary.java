package com.sap.sailing.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.Replicator;

class MediaLibrary {
    
/*    public class MediaTrack{
        
        MediaItem(boolean isMutable) {
            this.isMutable = isMutable;
        }
        
        public final boolean isMutable;
        public String id;
        public String title;
        public String url;
        public Date startTime;
        public int durationInMillis;
        public String mimeType;
    }*/

    private List<MediaTrack> mediaTracks;

    MediaLibrary(Replicator replicator) {
        this.mediaTracks = new CopyOnWriteArrayList<MediaTrack>();  
    }
    
    Collection<MediaTrack> findMediaTracksInTimeRange(Date startTime, Date endTime) {
        Collection<MediaTrack> result = new ArrayList<MediaTrack>();
        for (MediaTrack mediaTrack : mediaTracks) {
            long mediaStartTime = mediaTrack.startTime.getTime();
            long mediaEndTime = mediaStartTime + mediaTrack.durationInMillis;
            if (startTime.getTime() < mediaEndTime && mediaStartTime < endTime.getTime()) {
                result.add(mediaTrack);
            }
        }
        return result ;
    }

    void addMediaTrack(MediaTrack mediaTrack) {
        mediaTracks.add(mediaTrack);
    }

    void addMediaTracks(Collection<MediaTrack> mediaTracks) {
        this.mediaTracks.addAll(mediaTracks);
    }
    
    void deleteMediaTrack(MediaTrack mediaTrack) {
        mediaTracks.remove(mediaTrack);
    }

    void applyChanges(MediaTrack changedMediaTrack) {
        for (MediaTrack mediaTrack : mediaTracks) {
            if (mediaTrack.equals(changedMediaTrack)) {
                mediaTrack.title = changedMediaTrack.title;
                mediaTrack.url = changedMediaTrack.url;
                mediaTrack.startTime = changedMediaTrack.startTime;
                mediaTrack.durationInMillis = changedMediaTrack.durationInMillis;
                mediaTrack.mimeType = changedMediaTrack.mimeType;
                return;
            }
        }
    }

    Collection<MediaTrack> allTracks() {
        return new ArrayList<MediaTrack>(mediaTracks);
    }

    void serialize(ObjectOutputStream stream) throws IOException {
        stream.writeObject(mediaTracks);
    }

    @SuppressWarnings("unchecked")
    void deserialize(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        addMediaTracks((Collection<MediaTrack>) stream.readObject());
    }

}
