package com.sap.sailing.simulator.test.util;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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

public class TracTracWriter {
	
	private String[] sources;
	//Map<String, String> systemProperties;
	
	public TracTracWriter(String[] sources) {
		
		this.sources = sources;
		
	}
	
	public void write() throws Exception {
		
		System.setProperty("mongo.port", "10200");
	    System.setProperty("http.proxyHost", "proxy.wdf.sap.corp");
	    System.setProperty("http.proxyPort", "8080");
	    RacingEventServiceImpl service = new RacingEventServiceImpl();
	    URI liveURI = new URI("tcp://10.18.22.156:1520");
	    URI storedURI = new URI("tcp://10.18.22.156:1521");
	    
		for( String paramURLStr : sources ) {
			URL paramURL = new URL(paramURLStr);
		        //TODO: fix getTrackedRace
			RacesHandle raceHandle = null; //service.addTracTracRace(paramURL, liveURI, storedURI, EmptyWindStore.INSTANCE, 60000, this);
			synchronized (this) {
				this.wait();
		    }
			
			String regatta = raceHandle.getRegatta().getName();
			Set<RaceDefinition> races = raceHandle.getRaces();
			
	        List<TrackedRace> racesList = new ArrayList<TrackedRace>();
			
			for (RaceDefinition r : races) {
		        RaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta, r.getName());
		    //TODO: fix getTrackedRace
	            TrackedRace tr = null; //service.getExistingTrackedRace(raceIdentifier);	
	            racesList.add(tr);
			}
			System.out.println("start writing");
			//TODO: naming convention 
	        FileOutputStream f_os = new FileOutputStream(regatta + "race name" +".data");
	        ObjectOutputStream os = new ObjectOutputStream(f_os);
			os.writeObject(racesList);
			os.close();
			System.out.println("done writing");
			
		}
		
	}

	
}
