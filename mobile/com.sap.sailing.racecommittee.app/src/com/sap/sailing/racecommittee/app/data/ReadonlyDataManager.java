package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import android.app.LoaderManager.LoaderCallbacks;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface ReadonlyDataManager {

    public DataStore getDataStore();

    public LoaderCallbacks<DataLoaderResult<Collection<EventBase>>> createEventsLoader(
            LoadClient<Collection<EventBase>> callback);
    
    public LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createCourseAreasLoader(
            Serializable parentEventId, LoadClient<Collection<CourseArea>> callback);
    
    public LoaderCallbacks<DataLoaderResult<Collection<ManagedRace>>> createRacesLoader(
            Serializable courseAreaId, LoadClient<Collection<ManagedRace>> callback);
    
    public LoaderCallbacks<DataLoaderResult<Collection<Mark>>> createMarksLoader(
            ManagedRace managedRace, LoadClient<Collection<Mark>> callback);
    
    public LoaderCallbacks<DataLoaderResult<CourseBase>> createCourseLoader(
            ManagedRace managedRace, LoadClient<CourseBase> callback);
    
    public LoaderCallbacks<DataLoaderResult<Collection<Competitor>>> createCompetitorsLoader(
            ManagedRace managedRace, LoadClient<Collection<Competitor>> callback);
}
