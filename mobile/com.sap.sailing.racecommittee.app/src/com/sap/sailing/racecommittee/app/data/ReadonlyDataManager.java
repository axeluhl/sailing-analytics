package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.domain.CoursePosition;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.PositionListFragment;

/**
 * <p>
 * Interface for everything read related when working with data.
 * </p>
 * 
 * <p>
 * The result values of this interface's methods are meant to be used in conjunction with the Android's
 * {@link LoaderManager} facility. Everything that is loaded by this interface is stored and cached in the underlying
 * {@link DataStore}.
 * </p>
 * 
 * <p>
 * If there are cached results your {@link LoadClient}'s callback methods might be called twice (first cached results;
 * afterwards remote results). See {@link LoadClient} for restrictions that apply to your data handling code.
 * </p>
 */
public interface ReadonlyDataManager {

    /**
     * Gets the underlying data store.
     * 
     * @return the {@link DataStore}.
     */
    public DataStore getDataStore();

    /**
     * Creates a new {@link LoaderCallbacks} object for loading {@link EventBase}s.
     * 
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<EventBase>>> createEventsLoader(
            LoadClient<Collection<EventBase>> callback);

    /**
     * Creates a new {@link LoaderCallbacks} object for loading {@link CourseArea}s.
     * 
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createCourseAreasLoader(
            Serializable parentEventId, LoadClient<Collection<CourseArea>> callback);

    public LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createCourseAreasLoader(
            EventBase parentEvent, LoadClient<Collection<CourseArea>> callback);
    
    
    /**
     * Creates a new {@link LoaderCallbacks} object for loading racing referee positions.
     * 
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<CoursePosition>>> createPositionLoader(
			PositionListFragment positionListFragment);
    
    /**
     * Creates a new {@link LoaderCallbacks} object for loading {@link ManagedRace}s of a specific {@link CourseArea}.
     * 
     * @param courseAreaId
     *            the {@link CourseArea}'s Identifier.
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<ManagedRace>>> createRacesLoader(Serializable courseAreaId,
            LoadClient<Collection<ManagedRace>> callback);

    /**
     * Creates a new {@link LoaderCallbacks} object for loading a race's {@link Mark}s.
     * 
     * @param managedRace
     *            the {@link ManagedRace}.
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<Mark>>> createMarksLoader(ManagedRace managedRace,
            LoadClient<Collection<Mark>> callback);

    /**
     * Creates a new {@link LoaderCallbacks} object for loading a race's {@link CourseBase}.
     * 
     * @param managedRace
     *            the {@link ManagedRace}.
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<CourseBase>> createCourseLoader(ManagedRace managedRace,
            LoadClient<CourseBase> callback);

    /**
     * Creates a new {@link LoaderCallbacks} object for loading {@link Competitor}s.
     * 
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<Collection<Competitor>>> createCompetitorsLoader(ManagedRace managedRace,
            LoadClient<Collection<Competitor>> callback);
    
    /**
     * Creates a new {@link LoaderCallbacks} object for loading a client's configuration.
     * 
     * @param callback
     *            {@link LoadClient} implementing your data handling code.
     * @return {@link LoaderCallbacks} to be used in
     *         {@link LoaderManager#initLoader(int, android.os.Bundle, LoaderCallbacks)} or
     *         {@link LoaderManager#restartLoader(int, android.os.Bundle, LoaderCallbacks)}.
     */
    public LoaderCallbacks<DataLoaderResult<DeviceConfiguration>> createConfigurationLoader(DeviceConfigurationIdentifier identifier,
            LoadClient<DeviceConfiguration> callback);
}
