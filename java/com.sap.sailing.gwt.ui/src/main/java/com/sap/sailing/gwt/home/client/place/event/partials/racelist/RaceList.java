package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;

public class RaceList extends AbstractRaceList<LiveRaceDTO> {

    private final SortableRaceListColumn<LiveRaceDTO, ?> regattaNameColumn = RaceListColumnFactory.getRegattaNameColumn();
    private final SortableRaceListColumn<LiveRaceDTO, ?> flagsColumn = RaceListColumnFactory.getFlagsColumn();
    private final SortableRaceListColumn<LiveRaceDTO, ?> windSpeedColumn = RaceListColumnFactory.getWindSpeedColumn();
    private final SortableRaceListColumn<LiveRaceDTO, ?> windDirectionColumn = RaceListColumnFactory.getWindDirectionColumn();
    private final SortableRaceListColumn<LiveRaceDTO, ?> raceViewStateColumn = RaceListColumnFactory.getRaceViewStateColumn();

    public RaceList(EventView.Presenter presenter, boolean showRegattaDetails) {
        super(presenter);
        this.regattaNameColumn.setShowDetails(showRegattaDetails);
    }

    public void setListData(LiveRacesDTO data) {
        boolean hasFleets = data.hasFleets();
        this.fleetCornerColumn.setShowDetails(hasFleets);
        this.fleetNameColumn.setShowDetails(hasFleets);
        this.courseAreaColumn.setShowDetails(data.hasCourseAreas());
        this.courseColumn.setShowDetails(data.hasCourses());
        boolean hasWind = data.hasWind();
        this.windSpeedColumn.setShowDetails(hasWind);
        this.windDirectionColumn.setShowDetails(hasWind);
        setTableData(data.getRaces());
    }

    @Override
    protected void initTableColumns() {
        add(fleetCornerColumn);
        add(regattaNameColumn);
        add(raceNameColumn);
        add(fleetNameColumn);
        add(startTimeColumn);
        add(flagsColumn);
        add(windSpeedColumn);
        add(windDirectionColumn);
        add(courseAreaColumn);
        add(courseColumn);
        add(raceViewStateColumn);
        add(raceViewerButtonColumn);
    }
}
