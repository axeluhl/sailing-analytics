package com.sap.sailing.simulator.test.util;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.impl.TracTracAdapterFactoryImpl;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.simulator.impl.SimulatorUtils;

@SuppressWarnings("restriction")
public class TracTracReaderFromURLs implements TracTracReader {

    public void setMongoPortStr(String mongoPortStr) {
        this.mongoPortStr = mongoPortStr;
    }

    public void setProxyHostStr(String proxyHostStr) {
        this.proxyHostStr = proxyHostStr;
    }

    public void setProxyPortStr(String proxyPortStr) {
        this.proxyPortStr = proxyPortStr;
    }

    public void setLiveURIStr(String liveURIStr) {
        this.liveURIStr = liveURIStr;
    }

    public void setStoredUriStr(String storedUriStr) {
        this.storedURIStr = storedUriStr;
    }

    private String mongoPortStr = "10200";
    private String proxyHostStr = "proxy.wdf.sap.corp";
    private String proxyPortStr = "8080";
    private String liveURIStr = "tcp://10.18.22.156:1520";
    private String storedURIStr = "tcp://10.18.22.156:1521";

    private String[] tracTracParamURLs;

    public TracTracReaderFromURLs(String[] tracTracParamURLs) {
        super();
        this.tracTracParamURLs = tracTracParamURLs;
    }

    @Override
    public List<TrackedRace> read() throws Exception {
        System.setProperty("mongo.port", mongoPortStr);
        System.setProperty("http.proxyHost", proxyHostStr);
        System.setProperty("http.proxyPort", proxyPortStr);
        RacingEventServiceImpl service = new RacingEventServiceImpl();
        final TracTracAdapterFactory tracTracAdapterFactory = new TracTracAdapterFactoryImpl();
        URI liveURI = new URI(liveURIStr);
        URI storedURI = new URI(storedURIStr);
        List<TrackedRace> racesList = new ArrayList<TrackedRace>();
        for (String paramURLStr : tracTracParamURLs) {
            URL paramURL = new URL(paramURLStr);
            RaceHandle raceHandle = SimulatorUtils.loadRace(service, tracTracAdapterFactory, paramURL, liveURI, storedURI,
                    null, null, 60000);
            String regatta = raceHandle.getRegatta().getName();
            RaceDefinition r = raceHandle.getRace();
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, r.getName());
            TrackedRace tr = service.getExistingTrackedRace(raceIdentifier);
            tr.waitUntilNotLoading();
            racesList.add(tr);
        }
        return racesList;
    }

}
