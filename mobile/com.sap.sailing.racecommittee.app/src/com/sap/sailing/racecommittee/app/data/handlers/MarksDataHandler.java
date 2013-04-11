package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class MarksDataHandler extends DataHandler<Collection<Mark>> {

    public MarksDataHandler(OnlineDataManager manager, LoadClient<Collection<Mark>> client) {
        super(manager, client);
    }

    @Override
    public void onLoaded(Collection<Mark> data) {
        super.onLoaded(data);
        manager.addMarks(data);
    }

}
