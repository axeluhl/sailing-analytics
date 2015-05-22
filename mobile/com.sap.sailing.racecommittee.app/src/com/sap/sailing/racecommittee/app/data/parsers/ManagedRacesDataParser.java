package com.sap.sailing.racecommittee.app.data.parsers;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceRow;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.racecommittee.app.domain.FleetIdentifier;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.MergingRegattaConfigurationLoader;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceIdentifierImpl;
import com.sap.sailing.racecommittee.app.domain.impl.ManagedRaceImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

public class ManagedRacesDataParser implements DataParser<Collection<ManagedRace>> {

    private final JsonDeserializer<RaceGroup> deserializer;
    private final ConfigurationLoader<RegattaConfiguration> globalConfigurationLoader;
    private final AbstractLogEventAuthor author;

    public ManagedRacesDataParser(AbstractLogEventAuthor author, ConfigurationLoader<RegattaConfiguration> globalConfiguration,
            JsonDeserializer<RaceGroup> deserializer) {
        this.author = author;
        this.globalConfigurationLoader = globalConfiguration;
        this.deserializer = deserializer;
    }

    public Collection<ManagedRace> parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parse(reader);
        JSONArray jsonArray = Helpers.toJSONArraySafe(parsedResult);

        Collection<ManagedRace> managedRaces = new ArrayList<ManagedRace>();
        for (Object element : jsonArray) {
            JSONObject json = Helpers.toJSONObjectSafe(element);

            RaceGroup group = deserializer.deserialize(json);
            addManagedRaces(managedRaces, group);
        }

        return managedRaces;
    }

    private void addManagedRaces(Collection<ManagedRace> target, RaceGroup raceGroup) {
        for (SeriesWithRows series : raceGroup.getSeries()) {
            for (RaceRow raceRow : series.getRaceRows()) {
                Fleet fleet = raceRow.getFleet();
                for (RaceCell cell : raceRow.getCells()) {
                    ManagedRace race = createManagedRace(raceGroup, series, fleet, cell.getName(), cell.getRaceLog());
                    target.add(race);
                }
            }
        }
    }

    private ManagedRace createManagedRace(RaceGroup raceGroup, SeriesBase series, Fleet fleet, String name,
            RaceLog raceLog) {
        ConfigurationLoader<RegattaConfiguration> configurationLoader = globalConfigurationLoader;
        RegattaConfiguration localConfiguration = raceGroup.getRegattaConfiguration();
        if (localConfiguration != null) {
            configurationLoader = new MergingRegattaConfigurationLoader(localConfiguration, globalConfigurationLoader);
        }
        FleetIdentifier fleetIdentifier = new FleetIdentifierImpl(fleet, series, raceGroup);
        ManagedRaceIdentifier identifier = new ManagedRaceIdentifierImpl(name, fleetIdentifier);
        RaceState state = RaceStateImpl.create(raceLog, author, configurationLoader);
        return new ManagedRaceImpl(identifier, state);
    }

}
