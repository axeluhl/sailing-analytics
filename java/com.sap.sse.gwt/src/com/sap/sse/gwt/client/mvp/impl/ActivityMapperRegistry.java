package com.sap.sse.gwt.client.mvp.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/**
 * Allows parts of an application to register {@link ActivityMapper}s, implementing a composite
 * pattern for {@link ActivityMapper}. The mappers registered will be asked in their order of
 * registration for an activity for a {@link Place}. The first to provide a non-<code>null</code>
 * result tells the activity to be used.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ActivityMapperRegistry implements ActivityMapper {
    private final List<ActivityMapper> mappers;

    public ActivityMapperRegistry() {
        super();
        this.mappers = new ArrayList<>();
    }

    public void addActivityMapper(ActivityMapper mapper) {
        if (!mappers.contains(mapper)) {
            mappers.add(mapper);
        }
    }
    
    public void removeActivityMapper(ActivityMapper mapper) {
        mappers.remove(mapper);
    }
    
    /**
     * Map each Place to its corresponding Activity.
     */
    @Override
    public Activity getActivity(final Place place) {
        for (ActivityMapper mapper : mappers) {
            Activity activity = mapper.getActivity(place);
            if (activity != null) {
                return activity;
            }
        }
        return null;
    }
}
