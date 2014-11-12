package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.Callable;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.data.handlers.CompetitorsDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.CourseBaseHandler;
import com.sap.sailing.racecommittee.app.data.handlers.DataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.EventsDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.ManagedRacesDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.MarksDataHandler;
import com.sap.sailing.racecommittee.app.data.handlers.NullDataHandler;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderCallbacks;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderCallbacks.LoaderCreator;
import com.sap.sailing.racecommittee.app.data.loaders.DataLoaderResult;
import com.sap.sailing.racecommittee.app.data.loaders.ImmediateDataLoaderCallbacks;
import com.sap.sailing.racecommittee.app.data.loaders.OnlineDataLoader;
import com.sap.sailing.racecommittee.app.data.parsers.CompetitorsDataParser;
import com.sap.sailing.racecommittee.app.data.parsers.CourseBaseParser;
import com.sap.sailing.racecommittee.app.data.parsers.DataParser;
import com.sap.sailing.racecommittee.app.data.parsers.DeviceConfigurationParser;
import com.sap.sailing.racecommittee.app.data.parsers.EventsDataParser;
import com.sap.sailing.racecommittee.app.data.parsers.ManagedRacesDataParser;
import com.sap.sailing.racecommittee.app.data.parsers.MarksDataParser;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesRegattaConfigurationLoader;
import com.sap.sailing.racecommittee.app.services.sending.MessageSendingService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseBaseDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardGroupBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.NationalityJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PersonJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RegattaConfigurationJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TeamJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racegroup.impl.RaceGroupDeserializer;

/**
 * Enables accessing of data.
 */
public class OnlineDataManager extends DataManager {
    private static final String TAG = OnlineDataManager.class.getName();

    protected OnlineDataManager(Context context, DataStore dataStore, SharedDomainFactory domainFactory) {
        super(context, dataStore, domainFactory);
    }

    public void addEvents(Collection<EventBase> events) {
        for (EventBase event : events) {
            dataStore.addEvent(event);
        }
    }

    public void addRaces(Collection<ManagedRace> data) {
        for (ManagedRace race : data) {
            dataStore.addRace(race);
        }
    }

    public void addMarks(Collection<Mark> marks) {
        for (Mark mark : marks) {
            dataStore.addMark(mark);
        }
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<Collection<EventBase>>> createEventsLoader(
            LoadClient<Collection<EventBase>> callback) {
        return new DataLoaderCallbacks<Collection<EventBase>>(callback, new LoaderCreator<Collection<EventBase>>() {
            @Override
            public Loader<DataLoaderResult<Collection<EventBase>>> create(int id, Bundle args) throws Exception {
                DataParser<Collection<EventBase>> parser = new EventsDataParser(new EventBaseJsonDeserializer(
                        new VenueJsonDeserializer(new CourseAreaJsonDeserializer(domainFactory)), new LeaderboardGroupBaseJsonDeserializer()));
                DataHandler<Collection<EventBase>> handler = new EventsDataHandler(OnlineDataManager.this);

                ExLog.i(context, TAG, "getEventsLoader created new loader...");

                return new OnlineDataLoader<Collection<EventBase>>(context, 
                        new URL(preferences.getServerBaseURL() + "/sailingserver/events"), parser, handler);
            }
        }, getContext());
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<Collection<CourseArea>>> createCourseAreasLoader(
            final Serializable parentEventId, LoadClient<Collection<CourseArea>> callback) {
        return new ImmediateDataLoaderCallbacks<Collection<CourseArea>>(context, callback,
                new Callable<Collection<CourseArea>>() {
                    @Override
                    public Collection<CourseArea> call() throws Exception {
                        if (dataStore.hasEvent(parentEventId)) {
                            EventBase event = dataStore.getEvent(parentEventId);
                            return dataStore.getCourseAreas(event);
                        } else {
                            throw new DataLoadingException(String.format(
                                    "Unable to load course areas for unknown event %s.", parentEventId));
                        }
                    }
                });
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<Collection<ManagedRace>>> createRacesLoader(final Serializable courseAreaId,
            LoadClient<Collection<ManagedRace>> callback) {
        return new DataLoaderCallbacks<Collection<ManagedRace>>(callback, new LoaderCreator<Collection<ManagedRace>>() {
            @Override
            public Loader<DataLoaderResult<Collection<ManagedRace>>> create(int id, Bundle args) throws Exception {
                ConfigurationLoader<RegattaConfiguration> globalConfiguration = PreferencesRegattaConfigurationLoader.loadFromPreferences(preferences);
                
                DataParser<Collection<ManagedRace>> parser = new ManagedRacesDataParser(preferences.getAuthor(),
                        globalConfiguration, RaceGroupDeserializer.create(domainFactory, RegattaConfigurationJsonDeserializer.create()));
                DataHandler<Collection<ManagedRace>> handler = new ManagedRacesDataHandler(OnlineDataManager.this);
                return new OnlineDataLoader<Collection<ManagedRace>>(context, new URL(preferences.getServerBaseURL()
                    + "/sailingserver/rc/racegroups?"+
                    RaceLogServletConstants.PARAM_COURSE_AREA_FILTER + "=" + courseAreaId.toString()+"&"+
                    RaceLogServletConstants.PARAMS_CLIENT_UUID + "=" + MessageSendingService.uuid), parser, handler);
            }
        }, getContext());
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<Collection<Mark>>> createMarksLoader(final ManagedRace managedRace,
            LoadClient<Collection<Mark>> callback) {
        return new DataLoaderCallbacks<Collection<Mark>>(callback, new LoaderCreator<Collection<Mark>>() {
            @Override
            public Loader<DataLoaderResult<Collection<Mark>>> create(int id, Bundle args) throws Exception {
                JsonDeserializer<Mark> markDeserializer = new MarkDeserializer(domainFactory);
                DataParser<Collection<Mark>> parser = new MarksDataParser(markDeserializer);
                DataHandler<Collection<Mark>> handler = new MarksDataHandler(OnlineDataManager.this);

                ManagedRaceIdentifier identifier = managedRace.getIdentifier();

                String raceGroupName = URLEncoder.encode(identifier.getRaceGroup().getName());
                String raceColumnName = URLEncoder.encode(identifier.getRaceName());
                String fleetName = URLEncoder.encode(identifier.getFleet().getName());

                return new OnlineDataLoader<Collection<Mark>>(context, new URL(preferences.getServerBaseURL() + 
                        "/sailingserver/rc/marks?"+RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "=" + raceGroupName +
                        "&"+RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+"=" + raceColumnName + 
                        "&"+RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=" + fleetName), parser, handler);
            }
        }, getContext());
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<CourseBase>> createCourseLoader(final ManagedRace managedRace,
            LoadClient<CourseBase> callback) {
        return new DataLoaderCallbacks<CourseBase>(callback, new LoaderCreator<CourseBase>() {
            @Override
            public Loader<DataLoaderResult<CourseBase>> create(int id, Bundle args) throws Exception {
                JsonDeserializer<CourseBase> courseBaseDeserializer = new CourseBaseDeserializer(
                        new WaypointDeserializer(new ControlPointDeserializer(new MarkDeserializer(domainFactory),
                                new GateDeserializer(domainFactory, new MarkDeserializer(domainFactory)))));
                DataParser<CourseBase> parser = new CourseBaseParser(courseBaseDeserializer);
                DataHandler<CourseBase> handler = new CourseBaseHandler(OnlineDataManager.this, managedRace);

                ManagedRaceIdentifier identifier = managedRace.getIdentifier();

                String raceGroupName = URLEncoder.encode(identifier.getRaceGroup().getName());
                String raceColumnName = URLEncoder.encode(identifier.getRaceName());
                String fleetName = URLEncoder.encode(identifier.getFleet().getName());

                return new OnlineDataLoader<CourseBase>(context, new URL(preferences.getServerBaseURL()
                    + "/sailingserver/rc/currentcourse?" + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "="
                    + raceGroupName + "&" + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME + "=" + raceColumnName
                    + "&"+RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=" + fleetName), parser, handler);
            }
        }, getContext());
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<Collection<Competitor>>> createCompetitorsLoader(
            final ManagedRace managedRace, LoadClient<Collection<Competitor>> callback) {
        return new DataLoaderCallbacks<Collection<Competitor>>(callback, new LoaderCreator<Collection<Competitor>>() {
            @Override
            public Loader<DataLoaderResult<Collection<Competitor>>> create(int id, Bundle args) throws Exception {
                ExLog.i(context, TAG, String.format("Creating Competitor-OnlineDataLoader %d", id));
                JsonDeserializer<Competitor> competitorDeserializer = new CompetitorJsonDeserializer(domainFactory.getCompetitorStore(),
                        new TeamJsonDeserializer(new PersonJsonDeserializer(new NationalityJsonDeserializer(domainFactory))), 
                        new BoatJsonDeserializer(new BoatClassJsonDeserializer(domainFactory)));
                DataParser<Collection<Competitor>> parser = new CompetitorsDataParser(competitorDeserializer);
                DataHandler<Collection<Competitor>> handler = new CompetitorsDataHandler(OnlineDataManager.this,
                        managedRace);

                ManagedRaceIdentifier identifier = managedRace.getIdentifier();

                String raceGroupName = URLEncoder.encode(identifier.getRaceGroup().getName());
                String raceColumnName = URLEncoder.encode(identifier.getRaceName());
                String fleetName = URLEncoder.encode(identifier.getFleet().getName());

                return new OnlineDataLoader<Collection<Competitor>>(context, new URL(
                        preferences.getServerBaseURL() + "/sailingserver/rc/competitors?"
                                + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "=" + raceGroupName + "&"
                                + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME + "=" + raceColumnName + "&"
                                + RaceLogServletConstants.PARAMS_RACE_FLEET_NAME + "=" + fleetName), parser, handler);
            }
        }, getContext());
    }

    @Override
    public LoaderCallbacks<DataLoaderResult<DeviceConfiguration>> createConfigurationLoader(final DeviceConfigurationIdentifier identifier,
            LoadClient<DeviceConfiguration> callback) {
        return new DataLoaderCallbacks<DeviceConfiguration>(callback, new LoaderCreator<DeviceConfiguration>() {
            @Override
            public Loader<DataLoaderResult<DeviceConfiguration>> create(int id, Bundle args) throws Exception {
                ExLog.i(context, TAG, String.format("Creating Configuration-OnlineDataLoader %d", id));
                
                DataHandler<DeviceConfiguration> handler = new NullDataHandler<DeviceConfiguration>();
                DataParser<DeviceConfiguration> parser = new DeviceConfigurationParser(DeviceConfigurationJsonDeserializer.create());
                
                String encodedIdentifier = URLEncoder.encode(identifier.getClientIdentifier());
                encodedIdentifier = encodedIdentifier.replace("+", "%20");
                
                return new OnlineDataLoader<DeviceConfiguration>(
                        context, 
                        new URL(preferences.getServerBaseURL() + "/sailingserver/rc/configuration?client="+ encodedIdentifier), 
                        parser, handler);
            }
        }, getContext());
    }
}
