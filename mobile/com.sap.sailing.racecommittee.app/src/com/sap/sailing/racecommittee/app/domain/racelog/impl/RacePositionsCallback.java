package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.MapMarker;
import com.sap.sailing.racecommittee.app.domain.impl.MapMarkerImpl;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class RacePositionsCallback implements ServerReplyCallback {
    private static final String TAG = RacePositionsCallback.class.getName();
    private OnRaceUpdatedListener listener;

    public void register(OnRaceUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void processResponse(Context context, InputStream responseStream,
                                String raceId) {
        ReadonlyDataManager dataManager = DataManager.create(context);
        List<MapMarker> markersToAdd = parseResponse(context, dataManager,
                responseStream);
        addPositions(context, raceId, dataManager, markersToAdd);
    }

    protected List<MapMarker> parseResponse(Context context,
                                            ReadonlyDataManager dataManager, InputStream responseStream) {
        List<MapMarker> markersToAdd = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try {
            JSONObject raceItems = (JSONObject) parser
                    .parse(new InputStreamReader(responseStream));
            //ExLog.w(context, TAG, raceItems.toString());

            JSONArray marks = (JSONArray) raceItems.get("marks");
            for (int i = 0; i < marks.size(); ++i) {
                MapMarker mm = parseMarker((JSONObject) marks.get(i));
                markersToAdd.add(mm);
            }
        } catch (Exception e) {
            ExLog.e(context, TAG, "Error parsing server response");
            ExLog.e(context, TAG, e.getMessage());
        }
        return markersToAdd;
    }

    private MapMarker parseMarker(JSONObject jsonMarker) {
        String name = (String) jsonMarker.get("name");
        UUID id = UUID.fromString((String) jsonMarker.get("id"));

        ArrayList<MapMarker.TrackMeasurement> track = parseMarkerTrack((JSONArray) jsonMarker.get("track"));

        return new MapMarkerImpl(name, id, track);
    }

    private ArrayList<MapMarker.TrackMeasurement> parseMarkerTrack(JSONArray jsonTrack) {
        ArrayList<MapMarker.TrackMeasurement> ret = new ArrayList<>();
        for (int i = 0; i < jsonTrack.size(); ++i) {
            JSONObject trackPart = (JSONObject) jsonTrack.get(i);
            MapMarker.TrackMeasurement thisMarker = new MapMarker.TrackMeasurement();
            double lat = Double.parseDouble(trackPart.get("lat-deg").toString());
            double lng = Double.parseDouble(trackPart.get("lng-deg").toString());
            thisMarker.position = new LatLng(lat, lng);
            thisMarker.timepoint = (long) trackPart.get("timepoint-ms");
            ret.add(thisMarker);
        }

        return ret;
    }

    private void addPositions(Context context, Serializable raceId,
                              ReadonlyDataManager dataManager, List<MapMarker> markersToAdd) {
        if (markersToAdd.isEmpty()) {
            ExLog.i(context, TAG, "No server-side markers to add for race "
                    + raceId);
            return;
        }

        ExLog.i(context, TAG, String.format(
                "Server sent %d markers to be added for race %s.",
                markersToAdd.size(), raceId));

        if (!dataManager.getDataStore().hasRace(raceId)) {
            ExLog.w(context, TAG, "I have no race " + raceId);
            return;
        }

        // ADD STUFF

        ManagedRace race = dataManager.getDataStore().getRace(raceId);

        if (race == null) {
            ExLog.w(context, TAG, "Unable to retrieve race"
                    + raceId);
            return;
        }

        race.setMapMarkers(markersToAdd);

        if (listener != null) {
            listener.OnRaceUpdated(race);
        }
    }
}
