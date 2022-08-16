package com.sap.sailing.shared.server.operations;

import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class UpdateCourseTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -6752249733639856050L;
    protected final UUID uuid;
    protected final String name;
    protected final String shortName;
    protected final URL optionalImageURL;
    protected final ArrayList<String> tags;
    protected final Integer defaultNumberOfLaps;

    public UpdateCourseTemplateOperation(UUID uuid, String name, String shortName, URL optionalImageURL,
            ArrayList<String> tags, Integer defaultNumberOfLaps) {
        this.uuid = uuid;
        this.name = name;
        this.shortName = shortName;
        this.optionalImageURL = optionalImageURL;
        this.tags = tags;
        this.defaultNumberOfLaps = defaultNumberOfLaps;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalUpdateCourseTemplate(uuid, name, shortName, optionalImageURL, tags, defaultNumberOfLaps);
        return null;
    }

}
