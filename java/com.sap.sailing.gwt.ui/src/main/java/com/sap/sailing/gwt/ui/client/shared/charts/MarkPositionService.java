package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.shared.charts.RaceIdentifierToLeaderboardRaceColumnAndFleetMapper.LeaderboardNameRaceColumnNameAndFleetName;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public interface MarkPositionService {
    public static class MarkTrackDTO implements IsSerializable {
        private MarkDTO mark;
        private Iterable<GPSFixDTO> fixes;
        private boolean thinnedOut;
        
        MarkTrackDTO() {} // for GWT serialization
        
        public MarkTrackDTO(MarkDTO mark, Iterable<GPSFixDTO> fixes, boolean thinnedOut) {
            this.mark = mark;
            this.fixes = fixes;
            this.thinnedOut = thinnedOut;
        }

        public MarkDTO getMark() {
            return mark;
        }

        public Iterable<GPSFixDTO> getFixes() {
            return fixes;
        }

        public boolean isThinnedOut() {
            return thinnedOut;
        }
    }
    
    public static class MarkTracksDTO implements IsSerializable {
        private Iterable<MarkTrackDTO> tracks;
        
        MarkTracksDTO() {} // for GWT serialization
        
        public MarkTracksDTO(Iterable<MarkTrackDTO> tracks) {
            this.tracks = tracks;
        }

        public Iterable<MarkTrackDTO> getTracks() {
            return tracks;
        }
    }
    
    void getMarksInTrackedRace(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, AsyncCallback<Iterable<MarkDTO>> callback);
    
    // Jonas Dann: I had no great idea for handling of massive amount of fixes. An exception is no real option. I
    // think the fixes have to be thinned out somehow and maybe shown in more detail when zoomed in. The aggregation of
    // fixes is in my eyes no option, because then you have to define behaviour when moving them.
    // I had no remaining time to incorporate the thinned out variable in my code.
    void getMarkTracks(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, AsyncCallback<MarkTracksDTO> callback);
    
    void getMarkTrack(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, String markIdAsString, AsyncCallback<MarkTrackDTO> callback);
    
    /**
     * The service may decide whether a mark fix can be removed. It may, for example, be impossible to
     * cleanly remove a mark fix if a tracked race already exists and the mark fixes are already part of
     * the GPS fix track which currently does not support a remove operation. However, when only the
     * regatta log is the basis of the service and no tracked race exists yet, mark fixes may be removed
     * by revoking the device mappings.
     */
    void canRemoveMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO fix, AsyncCallback<Boolean> callback);
    
    void removeMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO fix, AsyncCallback<Void> callback);
    
    void addMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO newFix, AsyncCallback<Void> callback);
    
    void editMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO oldFix, Position newPosition, AsyncCallback<Void> callback);
}