package com.sap.sailing.simulator.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.simulator.test.util.TracTracReader;

public class TestTracTracReader {

	public static void main(String[] args) throws Exception {
		
		File dir = new File("C:\\Users\\i059829\\workspace\\sapsailingcapture\\java\\com.sap.sailing.simulator.test");
		String[] flist = dir.list(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
             { return filename.endsWith(".data"); }} );
		
		//System.out.println(flist[1]);
		TracTracReader ttreader = new TracTracReader(flist);
		
		List<TrackedRace> lst = ttreader.read();
		
		for( TrackedRace r : lst ) {
			RegattaAndRaceIdentifier id = r.getRaceIdentifier();
			System.out.println(id.getRegattaName() + " / " + id.getRaceName());
		}
	
		
	}
	
}
