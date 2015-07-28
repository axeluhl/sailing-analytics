package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.ALWAYS;
import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.LARGE;
import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.MEDIUM;
import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.NEVER;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListColumnFactory;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListDataUtil;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.CollectionResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;

public class RacesListLive extends Composite {

    private final RaceListLiveRaces raceList;
    private final RaceListContainer<LiveRaceDTO> raceListContainer;

    public RacesListLive(EventView.Presenter presenter, boolean showRegattaDetails) {
        raceList = new RaceListLiveRaces(presenter, showRegattaDetails);
        raceListContainer = new RaceListContainer<LiveRaceDTO>(StringMessages.INSTANCE.liveNow(), raceList);
        initWidget(raceListContainer);
        getElement().getStyle().setDisplay(Display.NONE);
    }

    public RefreshableWidget<CollectionResult<LiveRaceDTO>> getRefreshable() {
        return raceListContainer;
    }
    
    private class RaceListLiveRaces extends AbstractRaceList<LiveRaceDTO> {
        private final SortableRaceListColumn<LiveRaceDTO, ?> regattaNameColumn = RaceListColumnFactory.getRegattaNameColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> flagsColumn = RaceListColumnFactory.getFlagsColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> windSpeedColumn = RaceListColumnFactory.getWindSpeedColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseAreaColumn = RaceListColumnFactory.getCourseAreaColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseColumn = RaceListColumnFactory.getCourseColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> raceViewStateColumn = RaceListColumnFactory.getRaceViewStateColumn();

        public RaceListLiveRaces(EventView.Presenter presenter, boolean showRegattaDetails) {
            super(presenter);
            this.regattaNameColumn.setColumnVisibility(showRegattaDetails ? ALWAYS : NEVER);
        }
        
        @Override
        protected void setTableData(Collection<LiveRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setColumnVisibility(hasFleets ? ALWAYS : NEVER);
            this.fleetNameColumn.setColumnVisibility(hasFleets ? LARGE : NEVER);
            this.startTimeColumn.setShowTimeOnly(!RaceListDataUtil.hasDifferentStartDates(data));
            this.courseAreaColumn.setColumnVisibility(RaceListDataUtil.hasCourseAreas(data) ? LARGE : NEVER);
            this.courseColumn.setColumnVisibility(RaceListDataUtil.hasCourses(data) ? LARGE : NEVER);
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setColumnVisibility(hasWind ? ALWAYS : NEVER);
            this.windDirectionColumn.setColumnVisibility(hasWind ? MEDIUM : NEVER);
            super.setTableData(data);
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
}
