package com.sap.sailing.simulator.test.util;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

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
		// TODO Auto-generated method stub
		System.setProperty("mongo.port", mongoPortStr);
	    System.setProperty("http.proxyHost", proxyHostStr);
	    System.setProperty("http.proxyPort", proxyPortStr);
	    RacingEventServiceImpl service = new RacingEventServiceImpl();
	    URI liveURI = new URI(liveURIStr);
	    URI storedURI = new URI(storedURIStr);
	    
	    List<TrackedRace> racesList = new ArrayList<TrackedRace>();
	    
	    for( String paramURLStr : tracTracParamURLs ) {
	    	URL paramURL = new URL(paramURLStr);
	    	RacesHandle raceHandle = service.addTracTracRace(paramURL, liveURI, storedURI, EmptyWindStore.INSTANCE, 60000, this);
			synchronized (this) {
				this.wait();
		    }
			String regatta = raceHandle.getRegatta().getName();
			Set<RaceDefinition> races = raceHandle.getRaces();
			
			for (RaceDefinition r : races) {
		        RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, r.getName());
	            TrackedRace tr = service.getExistingTrackedRace(raceIdentifier);	
	            racesList.add(tr);
			}
	    }
	    
		return racesList;
	}

}
