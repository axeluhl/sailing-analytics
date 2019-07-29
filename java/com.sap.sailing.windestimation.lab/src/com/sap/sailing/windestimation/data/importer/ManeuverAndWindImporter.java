package com.sap.sailing.windestimation.data.importer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorTrackWithEstimationDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceWindJsonSerializer;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.WindQuality;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithManeuverForDataAnalysisPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithManeuverForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.RaceWithWindSourcesPersistenceManager;
import com.sap.sailing.windestimation.data.serialization.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.LabeledManeuverForEstimationJsonSerializer;
import com.sap.sailing.windestimation.data.serialization.ManeuverForDataAnalysisJsonSerializer;
import com.sap.sailing.windestimation.data.transformer.CompetitorTrackTransformer;
import com.sap.sailing.windestimation.data.transformer.CompleteManeuverCurveWithEstimationDataToLabelledManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.data.transformer.CompleteManeuverCurveWithEstimationDataToManeuverForDataAnalysisTransformer;
import com.sap.sailing.windestimation.util.LoggingUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverAndWindImporter {

    private static final int CONNECTION_TIMEOUT_MILLIS = 60000 * 5;
    public static final String REST_API_BASE_URL = "https://www.sapsailing.com/sailingserver/api/v1";
    public static final String REST_API_REGATTAS_PATH = "/regattas";
    public static final String REST_API_RACES_PATH = "/races";
    public static final String REST_API_ESTIMATION_DATA_PATH = "/completeManeuverCurvesWithEstimationData";
    public static final String REST_API_WIND_DATA_PATH = "/highQualityWindFixes";
    private final String startFromRegattaName = null;
    private final String startFromRegattaRace = null;
    private final RaceWithCompleteManeuverCurvePersistenceManager completeManeuverCurvePersistanceManager;
    private final RaceWithManeuverForDataAnalysisPersistenceManager maneuverForDataAnalysisPersistenceManager;
    private final RaceWithManeuverForEstimationPersistenceManager maneuverForEstimationPersistenceManager;
    private final RaceWithWindSourcesPersistenceManager raceWithWindSourcesPersistenceManager;
    private final CompleteManeuverCurveWithEstimationDataToManeuverForDataAnalysisTransformer maneuverForDataAnalysisTransformer;
    private final CompleteManeuverCurveWithEstimationDataToLabelledManeuverForEstimationTransformer maneuverForEstimationTransformer;
    private final ManeuverForDataAnalysisJsonSerializer maneuverForDataAnalysisJsonSerializer;
    private final LabeledManeuverForEstimationJsonSerializer maneuverForEstimationJsonSerializer;
    private boolean skipRace;
    private static final int NUMBER_OF_THREADS = 10; // high number due to HTTP requests
    private final ThreadPoolExecutor executorService;

    public ManeuverAndWindImporter() throws UnknownHostException {
        this.completeManeuverCurvePersistanceManager = new RaceWithCompleteManeuverCurvePersistenceManager();
        this.maneuverForDataAnalysisPersistenceManager = new RaceWithManeuverForDataAnalysisPersistenceManager();
        this.maneuverForEstimationPersistenceManager = new RaceWithManeuverForEstimationPersistenceManager();
        this.raceWithWindSourcesPersistenceManager = new RaceWithWindSourcesPersistenceManager();
        this.maneuverForDataAnalysisTransformer = new CompleteManeuverCurveWithEstimationDataToManeuverForDataAnalysisTransformer();
        this.maneuverForEstimationTransformer = new CompleteManeuverCurveWithEstimationDataToLabelledManeuverForEstimationTransformer();
        this.maneuverForDataAnalysisJsonSerializer = new ManeuverForDataAnalysisJsonSerializer();
        this.maneuverForEstimationJsonSerializer = new LabeledManeuverForEstimationJsonSerializer();
        this.executorService = new ThreadPoolExecutor(NUMBER_OF_THREADS, NUMBER_OF_THREADS, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(NUMBER_OF_THREADS));
        this.executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // this will block if the queue is full
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public HttpClient createNewHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT_MILLIS);
        HttpClient client = new SystemDefaultHttpClient(httpParams);
        client.getParams().setParameter("http.socket.timeout", CONNECTION_TIMEOUT_MILLIS);
        client.getParams().setParameter("http.connection.timeout", CONNECTION_TIMEOUT_MILLIS);
        client.getParams().setParameter("http.connection-manager.timeout", new Long(CONNECTION_TIMEOUT_MILLIS));
        client.getParams().setParameter("http.protocol.head-body-timeout", CONNECTION_TIMEOUT_MILLIS);
        return client;
    }

    public static void main(String[] args) throws Exception {
        ManeuverAndWindImporter importer = new ManeuverAndWindImporter();
        importer.importAllRegattas();
    }

    public void importAllRegattas() throws IllegalStateException, ClientProtocolException, IOException, ParseException,
            URISyntaxException, InterruptedException {
        skipRace = startFromRegattaName != null;
        LoggingUtil.logInfo("Importer for CompleteManeuverCurveWithEstimationData just started");
        LoggingUtil.logInfo("Dropping old database");
        if (!skipRace) {
            completeManeuverCurvePersistanceManager.dropCollection();
            maneuverForDataAnalysisPersistenceManager.dropCollection();
            maneuverForEstimationPersistenceManager.dropCollection();
            raceWithWindSourcesPersistenceManager.dropCollection();
        }
        LoggingUtil.logInfo("Fetching all existing regatta names");
        ImportStatistics importStatistics = new ImportStatistics();
        HttpGet getAllRegattas = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH);
        JSONArray regattasJson = (JSONArray) getJsonFromResponse(createNewHttpClient().execute(getAllRegattas));
        int numberOfRegattas = regattasJson.size();
        LoggingUtil.logInfo(numberOfRegattas + " regatta names have been fetched");
        int i = 0;
        for (Object regattaJson : regattasJson) {
            String regattaName = (String) ((JSONObject) regattaJson).get("name");
            LoggingUtil.logInfo("Processing regatta nr. " + ++i + "/" + numberOfRegattas + " ("
                    + Math.round(100.0 * i / numberOfRegattas) + "%): \"" + regattaName + "\"");
            importRegatta(regattaName, importStatistics);
        }
        executorService.shutdown();
        boolean success = executorService.awaitTermination(2, TimeUnit.HOURS);
        if (success) {
            LoggingUtil.logInfo("Import finished");
            synchronized (importStatistics) {
                importStatistics.regattasCount = regattasJson.size();
                logImportStatistics(importStatistics);
            }
        } else {
            new InterruptedException("Thread-pool was terminated after two hours waiting time");
        }
    }

    private void logImportStatistics(ImportStatistics importStatistics) {
        Duration duration = Duration.between(importStatistics.startTime, LocalDateTime.now());
        LoggingUtil.logInfo("Import statistics: \n\t" + importStatistics.regattasCount + " regattas\n\t"
                + importStatistics.racesCount + " races\n\t" + importStatistics.competitorTracksCount
                + " competitor tracks\n\t" + importStatistics.maneuversCount
                + " complete maneuver curves with estimation data\n\t" + importStatistics.racesWithHighQualityWindData
                + " races with high quality wind data\n\t" + importStatistics.ignoredRegattas
                + " ignored regatta due to error\n\t" + importStatistics.ingoredRaces
                + " ingored races due to error\n--------------------------------------------\nTime passed: "
                + duration.toHours() + "h " + (duration.toMinutes() - duration.toHours() * 60) + "m "
                + (duration.get(ChronoUnit.SECONDS) % 60) + "s");
    }

    private void importRegatta(String regattaName, ImportStatistics importStatistics)
            throws IllegalStateException, ClientProtocolException, IOException, ParseException, URISyntaxException {
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        HttpGet getRegatta = new HttpGet(REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName);
        JSONObject regattaJson = null;
        try {
            regattaJson = (JSONObject) getHttpResponseAsJson(regattaName, null, getRegatta);
        } catch (Exception e) {
            synchronized (importStatistics) {
                importStatistics.ignoredRegattas++;
            }
            LoggingUtil.logInfo("Error while processing regatta: " + regattaName + "\r\n" + getRegatta);
            return;
        }
        for (Object seriesJson : (JSONArray) regattaJson.get("series")) {
            JSONObject trackedRaces = (JSONObject) ((JSONObject) seriesJson).get("trackedRaces");
            JSONArray fleets = (JSONArray) trackedRaces.get("fleets");
            for (Object fleetJson : fleets) {
                JSONArray racesJson = (JSONArray) ((JSONObject) fleetJson).get("races");
                LoggingUtil.logInfo("Regatta contains " + racesJson.size() + " races");
                int i = 0;
                for (Object raceJson : racesJson) {
                    JSONObject race = (JSONObject) raceJson;
                    if ((boolean) race.get("isTracked") && !(boolean) race.get("isLive")
                            && (boolean) race.get("hasGpsData") && (boolean) race.get("hasWindData")) {
                        String trackedRaceName = (String) race.get("trackedRaceName");
                        i++;
                        final int raceNumber = i;
                        LoggingUtil.logInfo("Processing race nr. " + raceNumber + ": \"" + trackedRaceName + "\"");
                        executorService.execute(() -> {
                            try {
                                importRace(regattaName, trackedRaceName, importStatistics);
                            } catch (Exception e) {
                                synchronized (importStatistics) {
                                    importStatistics.ingoredRaces += 1;
                                }
                                String extraLog = "";
                                if (e instanceof HttpClientException) {
                                    extraLog = "\r\n" + ((HttpClientException) e).getRequest();
                                }
                                LoggingUtil.logInfo("Error while processing race nr. " + raceNumber + ": \""
                                        + trackedRaceName + "\"" + extraLog);
                            }
                        });
                    }
                }
                synchronized (importStatistics) {
                    importStatistics.racesCount += racesJson.size();
                }
            }
        }
    }

    private String encodeUrlPathPart(String urlPathPart) throws URISyntaxException, UnsupportedEncodingException {
        URI uri = new URI("http", "a.com", "/" + urlPathPart, null, null);
        String encodedUrlPath = uri.toString().substring("http://a.com/".length());
        String encodedUrlPathPart = encodedUrlPath.replaceAll("\\/", "__");
        return encodedUrlPathPart;
    }

    private void importRace(String regattaName, String trackedRaceName, ImportStatistics importStatistics)
            throws Exception {
        if (skipRace) {
            if (regattaName.equals(startFromRegattaName)
                    && (startFromRegattaRace == null || startFromRegattaRace.equals(trackedRaceName))) {
                skipRace = false;
            } else {
                return;
            }
        }
        String encodedRegattaName = encodeUrlPathPart(regattaName);
        String encodedRaceName = encodeUrlPathPart(trackedRaceName);
        String urlPath = REST_API_BASE_URL + REST_API_REGATTAS_PATH + "/" + encodedRegattaName + REST_API_RACES_PATH
                + "/" + encodedRaceName;
        HttpGet getEstimationData = new HttpGet(urlPath + REST_API_ESTIMATION_DATA_PATH);
        HttpGet getWindData = new HttpGet(urlPath + REST_API_WIND_DATA_PATH);
        JSONObject resultJson = getHttpResponseAsJson(regattaName, trackedRaceName, getEstimationData);
        parseManeuverData(regattaName, trackedRaceName, importStatistics, resultJson);
        resultJson = getHttpResponseAsJson(regattaName, trackedRaceName, getWindData);
        parseWindData(regattaName, trackedRaceName, importStatistics, resultJson);
    }

    private void parseWindData(String regattaName, String trackedRaceName, ImportStatistics importStatistics,
            JSONObject resultJson) {
        JSONArray windSourcesJson = (JSONArray) resultJson.get(RaceWindJsonSerializer.WIND_SOURCES);
        if (windSourcesJson != null) {
            raceWithWindSourcesPersistenceManager.add(regattaName, trackedRaceName, resultJson);
            long windFixesCount = 0;
            for (Object windSourceObj : windSourcesJson) {
                JSONArray windFixesJson = (JSONArray) ((JSONObject) windSourceObj).get(RaceWindJsonSerializer.FIXES);
                windFixesCount += windFixesJson.size();
            }
            LoggingUtil.logInfo("Imported " + windFixesCount + " wind fixes from " + windSourcesJson.size()
                    + " wind sources in race \"" + trackedRaceName + "\" of regatta \"" + regattaName + "\"");
            synchronized (importStatistics) {
                importStatistics.racesWithHighQualityWindData++;
            }
        } else {
            LoggingUtil.logInfo("No high quality wind fixes contained in race \"" + trackedRaceName + "\" of regatta \""
                    + regattaName + "\"");
        }
    }

    private void parseManeuverData(String regattaName, String trackedRaceName, ImportStatistics importStatistics,
            JSONObject resultJson) throws JsonDeserializationException {
        List<JSONObject> competitorTracks = new ArrayList<>();
        int maneuversCount = 0;
        CompetitorTrackWithEstimationDataJsonDeserializer<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationDataJsonDeserializer = completeManeuverCurvePersistanceManager
                .getNewCompetitorTrackWithEstimationDataJsonDeserializer();
        List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracksWithEstimationData = new ArrayList<>();
        WindQuality windQuality = WindQuality
                .values()[(int) ((long) resultJson.get(CompetitorTrackWithEstimationDataJsonSerializer.WIND_QUALITY))];
        for (Object competitorTrackJson : (JSONArray) resultJson
                .get(CompetitorTrackWithEstimationDataJsonSerializer.BYCOMPETITOR)) {
            JSONObject competitorTrack = (JSONObject) competitorTrackJson;
            JSONArray maneuverCurves = (JSONArray) competitorTrack
                    .get(CompetitorTrackWithEstimationDataJsonSerializer.ELEMENTS);
            if (!maneuverCurves.isEmpty()) {
                competitorTracks.add(competitorTrack);
                CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationData = competitorTrackWithEstimationDataJsonDeserializer
                        .deserialize(competitorTrack, regattaName, trackedRaceName);
                competitorTrack.put("clean",
                        competitorTrackWithEstimationData.isClean()
                                && competitorTrackWithEstimationData
                                        .getWaypointsCount() == competitorTrackWithEstimationData.getMarkPassingsCount()
                                && competitorTrackWithEstimationData.getMarkPassingsCount() > 1);
                competitorTracksWithEstimationData.add(competitorTrackWithEstimationData);
                maneuversCount += maneuverCurves.size();
            }
        }
        try {
            completeManeuverCurvePersistanceManager.addRace(regattaName, trackedRaceName, windQuality,
                    competitorTracks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addTransformedElementsToCompetitorTrackJson(competitorTracks, competitorTracksWithEstimationData,
                maneuverForDataAnalysisTransformer, maneuverForDataAnalysisJsonSerializer);
        try {
            maneuverForDataAnalysisPersistenceManager.addRace(regattaName, trackedRaceName, windQuality,
                    competitorTracks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addTransformedElementsToCompetitorTrackJson(competitorTracks, competitorTracksWithEstimationData,
                maneuverForEstimationTransformer, maneuverForEstimationJsonSerializer);
        try {
            maneuverForEstimationPersistenceManager.addRace(regattaName, trackedRaceName, windQuality,
                    competitorTracks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggingUtil.logInfo("Imported " + competitorTracks.size() + " competitor tracks with " + maneuversCount
                + " maneuvers in race \"" + trackedRaceName + "\" of regatta \"" + regattaName + "\"");
        synchronized (importStatistics) {
            importStatistics.competitorTracksCount += competitorTracks.size();
            importStatistics.maneuversCount += maneuversCount;
        }
    }

    private JSONObject getHttpResponseAsJson(String trackedRegattaName, String trackedRaceName,
            HttpGet getEstimationData) throws InterruptedException, Exception {
        HttpResponse httpResponse = null;
        Exception lastException = null;
        for (int i = 1; i <= 10; i++) {
            try {
                httpResponse = createNewHttpClient().execute(getEstimationData);
                JSONObject resultJson = (JSONObject) getJsonFromResponse(httpResponse);
                return resultJson;
            } catch (Exception e) {
                Thread.sleep(10000);
                lastException = e;
                if (trackedRaceName == null) {
                    LoggingUtil.logInfo("Connection error (" + i + "/10) while querying races of regatta \""
                            + trackedRegattaName + "\", retrying...");
                } else {
                    LoggingUtil.logInfo("Connection error (" + i + "/10) while processing race \"" + trackedRaceName
                            + "\" of regatta \"" + trackedRegattaName + "\", retrying...");
                }
            }
        }
        throw new HttpClientException(getEstimationData.toString(), lastException);
    }

    private <ToType> void addTransformedElementsToCompetitorTrackJson(List<JSONObject> competitorTracks,
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracksWithEstimationData,
            CompetitorTrackTransformer<CompleteManeuverCurveWithEstimationData, ToType> elementsTransformer,
            JsonSerializer<ToType> elementJsonSerializer) {
        List<CompetitorTrackWithEstimationData<ToType>> competitorTracksWithManeuvers = elementsTransformer
                .transform(competitorTracksWithEstimationData);
        Iterator<CompetitorTrackWithEstimationData<ToType>> competitorTracksWithManeuversIterator = competitorTracksWithManeuvers
                .iterator();
        for (JSONObject jsonCompetitorTrack : competitorTracks) {
            CompetitorTrackWithEstimationData<ToType> competitorTrackWithEstimationData = competitorTracksWithManeuversIterator
                    .next();
            JSONArray jsonManeuvers = new JSONArray();
            for (ToType maneuver : competitorTrackWithEstimationData.getElements()) {
                JSONObject jsonManeuver = elementJsonSerializer.serialize(maneuver);
                jsonManeuvers.add(jsonManeuver);
            }
            jsonCompetitorTrack.put(CompetitorTrackWithEstimationDataJsonSerializer.ELEMENTS, jsonManeuvers);
        }
    }

    public static Object getJsonFromResponse(HttpResponse response)
            throws IllegalStateException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        final Header contentEncoding = response.getEntity().getContentEncoding();
        final Reader reader;
        if (contentEncoding == null) {
            reader = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
        } else {
            reader = new InputStreamReader(response.getEntity().getContent(), contentEncoding.getValue());
        }
        Object json = jsonParser.parse(reader);
        reader.close();
        return json;
    }

    private static class ImportStatistics {
        private LocalDateTime startTime = LocalDateTime.now();
        private int regattasCount = 0;
        private int racesCount = 0;
        private int competitorTracksCount = 0;
        private int maneuversCount = 0;
        private int ignoredRegattas = 0;
        private int ingoredRaces = 0;
        private int racesWithHighQualityWindData = 0;
    }

    private static class HttpClientException extends Exception {

        private static final long serialVersionUID = 4948532287832868768L;
        private final String request;

        public HttpClientException(String request, Exception e) {
            super(e);
            this.request = request;
        }

        public String getRequest() {
            return request;
        }

    }

}
