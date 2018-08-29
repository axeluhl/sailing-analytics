package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType.StatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.CompetitorWithoutClubnameItemDescription;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic.SailorProfileNumericStatisticTypeFormater;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

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
            SingleEntry entry, FlagImageResolver flagImageResolver, StringMessages stringMessages) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();

        if (type.getAggregationType() != StatisticType.AVERAGE) {
            timeUi.setInnerText(SailorProfileNumericStatisticTypeFormater.format(entry.getRelatedTimePointOrNull()));
            timeLabelUi.setInnerText(stringMessages.time() + ": ");
            eventNameUi.setInnerText(entry.getLeaderboardNameOrNull() + " - " + entry.getRaceNameOrNull());
        } else {
            timeDivUi.removeFromParent();
            eventNameUi.removeFromParent();
        }

        valueLabelUi.setInnerText(
                SailorProfileNumericStatisticTypeFormater.getColumnHeadingName(type, stringMessages) + ": ");
        valueUi.setInnerText(SailorProfileNumericStatisticTypeFormater.format(type, entry.getValue(), stringMessages));
        competitorUi.add(new CompetitorWithoutClubnameItemDescription(competitor, flagImageResolver));
        clubNameUi.setInnerText(competitor.getName());
    }

}
