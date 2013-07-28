package com.sap.sailing.racecommittee.app.data.handlers;

import java.util.Collection;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;

public class MarksDataHandler extends DataHandler<Collection<Mark>> {

    public MarksDataHandler(OnlineDataManager manager, LoadClient<Collection<Mark>> client) {
        super(manager);
    }

    @Override
    public void onResult(Collection<Mark> data) {
        //super.onSuccess(data);
        manager.addMarks(data);
    }

}
