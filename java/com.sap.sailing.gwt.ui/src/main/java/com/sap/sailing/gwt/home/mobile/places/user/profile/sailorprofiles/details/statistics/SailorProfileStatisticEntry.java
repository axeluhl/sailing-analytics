package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.statistics;

import java.util.HashMap;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType.StatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.CompetitorWithoutClubnameItemDescription;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic.SailorProfileNumericStatisticTypeFormatter;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.Storage;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * displays a single statistic value for a single user on mobile, shows event name, the time of interest, the value of
 * the statistic, the name and club of the competitor, and a button to jump to the point in time in the race board
 */
public class SailorProfileStatisticEntry extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileStatisticEntry> {
    }

    @UiField
    DivElement eventNameUi;

    @UiField
    DivElement timeLabelUi;

    @UiField
    DivElement timeUi;

    @UiField
    DivElement valueLabelUi;

    @UiField
    DivElement timeDivUi;

    @UiField
    DivElement valueUi;

    @UiField
    FlowPanel competitorUi;

    @UiField
    DivElement clubNameUi;

    @UiField
    Button showPointInTimeButtonUi;

    public SailorProfileStatisticEntry(SailorProfileNumericStatisticType type, SimpleCompetitorWithIdDTO competitor,
            SingleEntry entry, FlagImageResolver flagImageResolver, StringMessages stringMessages,
            String serializedDataMiningQuery) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();

        if (type.getAggregationType() != StatisticType.AVERAGE) {
            timeUi.setInnerText(SailorProfileNumericStatisticTypeFormatter.format(entry.getRelatedTimePointOrNull()));
            timeLabelUi.setInnerText(stringMessages.time() + ": ");
            eventNameUi.setInnerText(entry.getLeaderboardNameOrNull() + " - " + entry.getRaceNameOrNull());
            showPointInTimeButtonUi.addClickHandler(e -> showInRaceboard(entry, type, competitor.getIdAsString()));
        } else {
            timeDivUi.removeFromParent();
            eventNameUi.removeFromParent();
            showPointInTimeButtonUi.setText(StringMessages.INSTANCE.showInDataMining());
            showPointInTimeButtonUi.addClickHandler(e -> handleLocalStorage(serializedDataMiningQuery));
        }

        valueLabelUi.setInnerText(
                SailorProfileNumericStatisticTypeFormatter.getColumnHeadingName(type, stringMessages) + ": ");
        valueUi.setInnerText(SailorProfileNumericStatisticTypeFormatter.format(type, entry.getValue(), stringMessages));
        competitorUi.add(new CompetitorWithoutClubnameItemDescription(competitor, flagImageResolver));
        clubNameUi.setInnerText(competitor.getName());
    }

    private void handleLocalStorage(String serializedDataMiningQuery) {
        final String identifier = SailingSettingsConstants.DATAMINING_QUERY_PREFIX + UUID.randomUUID().toString();
        String navigationUrl = "DataMining.html?q=" + identifier;
        if (Storage.isLocalStorageSupported() && serializedDataMiningQuery != null) {

            JSONObject json = new JSONObject();
            json.put("payload", new JSONString(serializedDataMiningQuery));
            json.put("creation", new JSONString("" + MillisecondsTimePoint.now().asMillis()));

            Storage store = Storage.getLocalStorageIfSupported();
            store.setItem(identifier, json.toString());
        }
        Window.Location.assign(navigationUrl);
    }

    private void showInRaceboard(final SingleEntry entry, final SailorProfileNumericStatisticType type,
            final String competitorId) {
        final RegattaAndRaceIdentifier raceIdentifier = entry.getRelatedRaceOrNull();

        // create raceboard context
        RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(raceIdentifier.getRegattaName(),
                raceIdentifier.getRaceName(), entry.getLeaderboardNameOrNull(), entry.getLeaderboardGroupNameOrNull(),
                entry.getEventIdOrNull(), type.getPlayerMode().name(), competitorId);
        RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = new RaceBoardPerspectiveOwnSettings(
                new MillisecondsDurationImpl(entry.getRelatedTimePointOrNull().asMillis()
                        - entry.getRelatedRaceStartTimePointOrNull().asMillis()));

        // create raceboard settings
        HashMap<String, Settings> innerSettings = new HashMap<>();
        innerSettings.put(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true));
        PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                perspectiveOwnSettings, innerSettings);
        String targetUrl = EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext, settings);

        Window.Location.assign(targetUrl);
    }

}
