package com.sap.sailing.domain.tractracadapter;

import java.util.List;

public interface JSONService {

    List<RaceRecord> getRaceRecords();

    String getEventName();

}
