package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeDTO;
import com.sap.sailing.gwt.ui.shared.WindLatticeGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;


@RemoteServiceRelativePath("simulator")
public interface SimulatorService extends RemoteService {

    public PositionDTO[] getRaceLocations();

    public WindLatticeDTO getWindLatice(WindLatticeGenParamsDTO params);

    public WindFieldDTO getWindField(WindFieldGenParamsDTO params);

    public PathDTO[] getPaths(WindFieldGenParamsDTO params);

    public List<WindPatternDTO> getWindPatterns();
    
    public WindPatternDisplay getWindPatternDisplay(WindPatternDTO pattern);

    public BoatClassDTO[] getBoatClasses();
}
