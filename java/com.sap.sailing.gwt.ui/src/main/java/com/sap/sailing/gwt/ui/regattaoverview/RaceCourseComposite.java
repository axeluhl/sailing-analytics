package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

public class RaceCourseComposite extends Composite {

    private final CellTable<WaypointDTO> waypointTable;
    private ListDataProvider<WaypointDTO> waypointDataProvider;
    private final SimplePanel mainPanel;

    @SuppressWarnings("unused")
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    
    private RegattaOverviewEntryDTO race;

    private static RegattaRaceStatesTableResources tableRes = GWT.create(RegattaRaceStatesTableResources.class);
    
    private static final String STYLE_NAME_PREFIX = "RegattaOverview-";
    private static final String STYLE_WORD_WRAP = STYLE_NAME_PREFIX + "wordWrap";

    public RaceCourseComposite(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        
        this.race = null;

        mainPanel = new SimplePanel();
        VerticalPanel panel = new VerticalPanel();
        mainPanel.setWidget(panel);
        panel.setWidth("100%");

        waypointDataProvider = new ListDataProvider<WaypointDTO>();
        waypointTable = createCourseDesignTable();

        panel.add(waypointTable);

        initWidget(mainPanel);
        
    }

    private CellTable<WaypointDTO> createCourseDesignTable() {
        CellTable<WaypointDTO> table = new CellTable<WaypointDTO>(/* pageSize */10000, tableRes);
        waypointDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        
        TextColumn<WaypointDTO> waypointNameColumn = new TextColumn<WaypointDTO>() {
            @Override
            public String getValue(WaypointDTO waypointDTO) {
                String result = waypointDTO.getName();
                result += (waypointDTO.passingSide == null) ? "" : ", to " + getNauticalSideAsText(waypointDTO.passingSide);
                return result;
            }
        };

        table.addColumn(waypointNameColumn, stringMessages.waypoints());
        waypointNameColumn.setCellStyleNames(STYLE_WORD_WRAP);

        return table;
    }

    public void setRace(RegattaOverviewEntryDTO regattaOverviewEntryDTO) {
        this.race = regattaOverviewEntryDTO;
        updateCourseDetails();
    }

    private void updateCourseDetails() {
        if (this.race != null && this.race.raceInfo.lastCourseDesign != null) {
            waypointDataProvider.getList().clear();
            waypointDataProvider.getList().addAll(this.race.raceInfo.lastCourseDesign.waypoints);
        }
    }
    
    private String getNauticalSideAsText(NauticalSide passingSide) {
        switch (passingSide) {
        case PORT:
            return stringMessages.portSide();
        case STARBOARD:
            return stringMessages.starboardSide();
        default:
            return "";
        }
    }
}
