package com.sap.sailing.server.gateway.windimport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter.WindImportResult.RaceEntry;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Util;

public abstract class AbstractWindImporter {
    public static class UploadRequest {
        public String boatId;
        public final List<FileItem> files = new ArrayList<FileItem>();
        public List<RegattaAndRaceIdentifier> races = new ArrayList<RegattaAndRaceIdentifier>();;
    }

    public static class WindImportResult {
        private Date first;
        private Date last;
        public String error;

        public final List<RaceEntry> raceEntries = new ArrayList<RaceEntry>();

        public Date getFirst() {
            return first;
        }

        public Date getLast() {
            return last;
        }

        public void update(Wind newWind) {
            Date newDate = newWind.getTimePoint().asDate();
            if (this.first == null || newDate.before(this.first)) {
                this.first = newDate;
            }
            if (this.last == null || newDate.after(this.last)) {
                this.last = newDate;
            }
        }

        RaceEntry addRaceEntry(String regattaName, String raceName) {
            RaceEntry raceEntry = new RaceEntry(regattaName, raceName);
            raceEntries.add(raceEntry);
            return raceEntry;
        }

        static class RaceEntry {
            public final String regattaName;
            public final String raceName;
            private int count;
            private Date first;
            private Date last;

            private RaceEntry(String regattaName, String raceName) {
                this.regattaName = regattaName;
                this.raceName = raceName;
            }

            public void update(Wind newWind) {
                count++;
                Date newDate = newWind.getTimePoint().asDate();
                if (this.first == null || newDate.before(this.first)) {
                    this.first = newDate;
                }
                if (this.last == null || newDate.after(this.last)) {
                    this.last = newDate;
                }
            }

            public int getCount() {
                return count;
            }

            public Date getFirst() {
                return first;
            }

            public Date getLast() {
                return last;
            }

            private JSONObject json() {
                JSONObject result = new JSONObject();
                result.put("regattaName", regattaName);
                result.put("raceName", raceName);
                result.put("count", getCount());
                if (getFirst() != null) {
                    result.put("first", getFirst().getTime());
                }
                if (getLast() != null) {
                    result.put("last", getLast().getTime());
                }
                return result;
            }
        }

        public JSONObject json() {
            JSONObject result = new JSONObject();
            if (getFirst() != null) {
                result.put("first", getFirst().getTime());
            }
            if (getLast() != null) {
                result.put("last", getLast().getTime());
            }
            result.put("error", error);
            JSONArray raceEntriesJson = new JSONArray();
            for (RaceEntry raceEntry : raceEntries) {
                if (raceEntry.count > 0) {
                    raceEntriesJson.add(raceEntry.json());
                }
            }
            result.put("raceEntries", raceEntriesJson);
            return result;
        }
    }

    public void importWindForUploadRequest(RacingEventService service, WindImportResult windImportResult, UploadRequest uploadRequest)
            throws IOException, InterruptedException, FormatNotSupportedException {
        WindSource windSource = getWindSource(uploadRequest);
        List<DynamicTrackedRace> trackedRaces = new ArrayList<DynamicTrackedRace>();
        if (uploadRequest.races.size() > 0) {
            for (RegattaAndRaceIdentifier raceEntry : uploadRequest.races) {
                DynamicTrackedRace trackedRace = service.getTrackedRace(raceEntry);
                if (trackedRace != null) {
                    trackedRaces.add(trackedRace);
                }
            }
        } else {
            for (Regatta regatta : service.getAllRegattas()) {
                for (RaceDefinition raceDefinition : regatta.getAllRaces()) {
                    trackedRaces.add(service.getTrackedRegatta(regatta).getTrackedRace(raceDefinition));
                }
            }
        }
        final Map<InputStream, String> streamsWithFilenames = new HashMap<>(); 
        for (FileItem file : uploadRequest.files) {
            streamsWithFilenames.put(file.getInputStream(), file.getName());
        }
        importWindToWindSourceAndTrackedRaces(service, windImportResult, windSource, trackedRaces, streamsWithFilenames);
    }

    public void importWindToWindSourceAndTrackedRaces(RacingEventService service, WindImportResult windImportResult, WindSource windSource,
            List<DynamicTrackedRace> trackedRaces, final Map<InputStream, String> streamsWithFilenames)
            throws IOException, InterruptedException, FormatNotSupportedException {
        Iterable<Wind> windFixes = importWind(streamsWithFilenames);
        if (!Util.isEmpty(windFixes)) {
            for (DynamicTrackedRace trackedRace : trackedRaces) {
                RegattaAndRaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
                RaceEntry raceEntry = windImportResult.addRaceEntry(raceIdentifier.getRegattaName(),
                        raceIdentifier.getRaceName());
                for (Wind wind : windFixes) {
                    windImportResult.update(wind);
                    if (trackedRace.recordWind(wind, windSource)) {
                        raceEntry.update(wind);
                    }
                }
                service.getPolarDataService().insertExistingFixes(trackedRace);
            }
        }
    }

    protected abstract Iterable<Wind> importWind(Map<InputStream, String> streamsWithFilenames)
            throws IOException, InterruptedException, FormatNotSupportedException;

    protected abstract WindSource getWindSource(UploadRequest uploadRequest);
}
