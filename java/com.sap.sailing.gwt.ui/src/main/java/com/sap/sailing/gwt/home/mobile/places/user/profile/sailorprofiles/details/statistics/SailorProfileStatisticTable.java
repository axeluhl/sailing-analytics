package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details.statistics;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.SailingPermissionsForRoleProvider;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType.StatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordionResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic.SailorProfileNumericStatisticTypeFormatter;
import com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.SailorProfileMobileResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.DataMiningQueryForSailorProfilesPersistor;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.security.ui.client.UserService;

/** displays a single statistic table which contains all {@link SailorProfileStatsiticEntry} objects */
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

    @UiField
    DivElement sectionTitleIconUi;

    public SailorProfileStatisticTable(SailorProfileNumericStatisticType type, SailorProfileStatisticDTO statistic,
            FlagImageResolver flagImageResolver, StringMessages stringMessages, final UserService userService) {
        initWidget(uiBinder.createAndBindUi(this));
        SailorProfileMobileResources.INSTANCE.css().ensureInjected();
        this.sectionTitleUi
                .setInnerText(SailorProfileNumericStatisticTypeFormatter.getDisplayName(type, stringMessages));

        // add icon
        Image icon = new Image();
        icon.setUrl(SailorProfileNumericStatisticTypeFormatter.getIcon(type));
        icon.setSize("auto", "2em");
        this.sectionTitleIconUi.appendChild(icon.getElement());

        // add SailorProfileStatisticTableEntry objects
        Set<Entry<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>>> entrySet = statistic.getResult().entrySet();

        // sort entries
        ArrayList<Entry<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>>> data = new ArrayList<>();
        data.addAll(entrySet);
        data.sort((o1, o2) -> Double.compare(o1.getValue().size() > 0 ? o1.getValue().get(0).getValue() : 0,
                o2.getValue().size() > 0 ? o2.getValue().get(0).getValue() : 0));

        boolean hasData = false;
        for (Entry<SimpleCompetitorWithIdDTO, ArrayList<SailorProfileStatisticDTO.SingleEntry>> entry : data) {
            hasData |= !entry.getValue().isEmpty();

            for (SingleEntry singleEntry : entry.getValue()) {
                contentContainerStatistic.add(new SailorProfileStatisticEntry(type, entry.getKey(), singleEntry,
                        flagImageResolver, stringMessages, statistic.getDataMiningQuery()));
            }
        }
        if (!hasData) {
            final Label lblEmpty = new Label(stringMessages.noStatisticsFoundForCompetitors());
            lblEmpty.addStyleName(DesktopAccordionResources.INSTANCE.css().accordionEmptyMessage());

            contentContainerStatistic.add(lblEmpty);
        }
        if (type.getAggregationType() == StatisticType.AVERAGE && userService.getCurrentUser().hasPermission(
                Permission.DATA_MINING.getStringPermission(), SailingPermissionsForRoleProvider.INSTANCE)) {
            addDataminingButton(statistic);
        }
    }

    /** adds a button to data mining for the average statistic */
    private void addDataminingButton(SailorProfileStatisticDTO statistic) {
        Button button = new Button();
        button.addStyleName(SailorProfileMobileResources.INSTANCE.css().showLeaderboardButton() + " "
                + SharedSailorProfileResources.INSTANCE.css().inverseButton());
        button.setText(StringMessages.INSTANCE.showInDataMining());
        button.addClickHandler(e -> handleLocalStorage(statistic));
        contentContainerStatistic.add(button);
    }

    /** store serialized data mining query into user store */
    private void handleLocalStorage(SailorProfileStatisticDTO dto) {
        final String identifier = SailingSettingsConstants.DATAMINING_QUERY_PREFIX + UUID.randomUUID().toString();
        DataMiningQueryForSailorProfilesPersistor.writeDMQueriesToLocalStorageIfPossible(dto, identifier);
        Window.Location.assign("DataMining.html?q=" + identifier);
    }

}
