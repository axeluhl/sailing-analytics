package com.sap.sailing.simulator.impl;

import java.io.IOException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.PolarDiagramFactory;

public class PolarDiagramFactoryImpl implements PolarDiagramFactory {

	@Override
	public PolarDiagram getPolarDiagram(BoatClass boatClass) {
		// TODO generalize to dependency on boat class; this is just the first hard-coded example
		String csvFilePath;
		if (boatClass.getDisplayName().equals(BoatClassMasterdata._49ER.getDisplayName())) {
			csvFilePath = "PolarDiagram49STG.csv";
		} else {
			return null;
		}
        PolarDiagram polarDiagram = null;
        try {
            polarDiagram = new PolarDiagramCSV(csvFilePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return polarDiagram;
	}

}
