package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateEvent extends AbstractEventOperation<Void> {
    private static final long serialVersionUID = -8271559266421161532L;
    private final String venueName;
    private final String publicationUrl;
    private final boolean isPublic;
    private final Serializable id;
    private final List<String> regattaNames;
    private final String eventName;

    public UpdateEvent(Serializable id, String eventName, String venueName, String publicationUrl, boolean isPublic, List<String> regattaNames) {
        super(id);
        this.eventName = eventName;
        this.id = id;
        this.venueName = venueName;
        this.publicationUrl = publicationUrl;
        this.isPublic = isPublic;
        this.regattaNames = regattaNames;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.updateEvent(id, eventName, venueName, publicationUrl, isPublic, regattaNames);
        return null;
    }
}
