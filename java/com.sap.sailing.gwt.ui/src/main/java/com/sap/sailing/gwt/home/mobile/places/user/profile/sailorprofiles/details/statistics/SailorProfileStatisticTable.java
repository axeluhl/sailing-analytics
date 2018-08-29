package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.statistics;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.statistic.SailorProfileNumericStatisticTypeFormater;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SailorProfileStatisticTable extends Composite {

    private static SailorProfileOverviewEntryUiBinder uiBinder = GWT.create(SailorProfileOverviewEntryUiBinder.class);

    interface SailorProfileOverviewEntryUiBinder extends UiBinder<Widget, SailorProfileStatisticTable> {
    }

    @UiField
    DivElement sectionTitleUi;

    @UiField
    HTMLPanel sectionTitleContainerUi;

    @UiField
    HTMLPanel contentContainerStatistic;

    public SailorProfileStatisticTable(SailorProfileNumericStatisticType type, SailorProfileStatisticDTO statistic,
            FlagImageResolver flagImageResolver, StringMessages stringMessages) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        this.sectionTitleUi
                .setInnerText(SailorProfileNumericStatisticTypeFormater.getDisplayName(type, stringMessages));

        for (Entry<SimpleCompetitorWithIdDTO, ArrayList<SailorProfileStatisticDTO.SingleEntry>> entry : statistic
                .getResult().entrySet()) {
            for (SingleEntry singleEntry : entry.getValue()) {
                contentContainerStatistic.add(new SailorProfileStatisticEntry(type, entry.getKey(), singleEntry,
                        flagImageResolver, stringMessages));
            }
        }
    }

}
