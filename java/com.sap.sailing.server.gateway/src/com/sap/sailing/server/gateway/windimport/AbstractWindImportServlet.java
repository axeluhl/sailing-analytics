package com.sap.sailing.server.gateway.windimport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet.WindImportResult.RaceEntry;
import com.sap.sse.common.Util;

public abstract class AbstractWindImportServlet extends SailingServerHttpServlet {
    private static final Logger logger = Logger.getLogger(AbstractWindImportServlet.class.getName());
    private static final long serialVersionUID = 1L;

    public static class UploadRequest {
        public String boatId;
        public final List<FileItem> files = new ArrayList<FileItem>();
        public List<RegattaAndRaceIdentifier> races = new ArrayList<RegattaAndRaceIdentifier>();;
    }

    static class WindImportResult {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        WindImportResult windImportResult = new WindImportResult();
        try {
            UploadRequest uploadRequest = readRequest(request);
            WindSource windSource = getWindSource(uploadRequest);
            List<DynamicTrackedRace> trackedRaces = new ArrayList<DynamicTrackedRace>();
            if (uploadRequest.races.size() > 0) {
                for (RegattaAndRaceIdentifier raceEntry : uploadRequest.races) {
                    DynamicTrackedRace trackedRace = getService().getTrackedRace(raceEntry);
                    if (trackedRace != null) {
                        trackedRaces.add(trackedRace);
                    }
                }
            } else {
                for (Regatta regatta : getService().getAllRegattas()) {
                    for (RaceDefinition raceDefinition : regatta.getAllRaces()) {
                        trackedRaces.add(getService().getTrackedRegatta(regatta).getTrackedRace(raceDefinition));
                    }
                }
            }
            final Map<InputStream, String> streamsWithFilenames = new HashMap<>(); 
            for (FileItem file : uploadRequest.files) {
                streamsWithFilenames.put(file.getInputStream(), file.getName());
            }
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
                    getService().getPolarDataService().insertExistingFixes(trackedRace);
                }
            }
            // Use text/html to prevent browsers from wrapping the response body,
            // see "Handling File Upload Responses in GWT" at http://www.artofsolving.com/node/50
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error ocurred trying to import wind fixes", e);
            windImportResult.error = e.toString();
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().append(windImportResult.json().toJSONString());
    }

    protected abstract Iterable<Wind> importWind(Map<InputStream, String> streamsWithFilenames) throws IOException, InterruptedException;

    protected abstract WindSource getWindSource(UploadRequest uploadRequest);

    private UploadRequest readRequest(HttpServletRequest req) throws FileUploadException, ParseException {
        UploadRequest result = new UploadRequest();
        // http://commons.apache.org/fileupload/using.html
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem item : items) {
            if (item.isFormField() && (item.getString() != null) && (item.getString().trim().length() > 0)) {
                if ("boatId".equals(item.getFieldName())) {
                    result.boatId = item.getString().trim();
                } else if ("races".equals(item.getFieldName())) {
                    JSONArray races = (JSONArray) new JSONParser().parse(item.getString().trim());
                    for (Object raceEntry : races) {
                        result.races.add(new RegattaNameAndRaceName((String) ((JSONObject) raceEntry).get("regatta"),
                                (String) ((JSONObject) raceEntry).get("race")));
                    }
                }
            } else if (item.getSize() > 0) {
                result.files.add(item);
            }
        }
        return result;
    }
}
