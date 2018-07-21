package com.sap.sailing.gwt.home.desktop.partials.liveraces;

import java.util.Collection;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.desktop.partials.racelist.AbstractRaceList;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListColumnFactory;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListColumnSet;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListContainer;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListDataUtil;
import com.sap.sailing.gwt.home.desktop.partials.racelist.SortableRaceListColumn;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.dispatch.shared.commands.CollectionResult;

public class LiveRacesList extends Composite {

    private final RaceListLiveRaces raceList;
    private final RaceListContainer<LiveRaceDTO> raceListContainer;

    public LiveRacesList(EventView.Presenter presenter, boolean showRegattaDetails) {
        raceList = new RaceListLiveRaces(presenter, showRegattaDetails);
        raceListContainer = new RaceListContainer<LiveRaceDTO>(StringMessages.INSTANCE.liveNow(), raceList);
        initWidget(raceListContainer);
        getElement().getStyle().setDisplay(Display.NONE);
    }

    public RefreshableWidget<CollectionResult<LiveRaceDTO>> getRefreshable() {
        return raceListContainer;
    }
    
    public AbstractRaceList<LiveRaceDTO> getRaceList() {
        return raceList;
    }
    
    private class RaceListLiveRaces extends AbstractRaceList<LiveRaceDTO> {
        private final SortableRaceListColumn<LiveRaceDTO, ?> regattaNameColumn = RaceListColumnFactory.getRegattaNameColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> flagsColumn = RaceListColumnFactory.getFlagsColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> windSpeedColumn = RaceListColumnFactory.getWindSpeedColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseAreaColumn = RaceListColumnFactory.getCourseAreaColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseColumn = RaceListColumnFactory.getCourseColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> raceViewStateColumn = RaceListColumnFactory.getRaceViewStateColumn();
        
        public RaceListLiveRaces(EventView.Presenter presenter, boolean showRegattaDetails) {
            super(presenter, new RaceListColumnSet(1, 1), false);
            this.regattaNameColumn.setShowDetails(showRegattaDetails);
        }
        
        @Override
        protected SortableRaceListColumn<LiveRaceDTO, ?> getDefaultSortColumn() {
            return startTimeColumn;
        }
        
        @Override
        protected void setTableData(Collection<LiveRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setShowDetails(hasFleets);
            this.fleetNameColumn.setShowDetails(hasFleets);
            this.startTimeColumn.setShowTimeOnly(!RaceListDataUtil.hasDifferentStartDates(data));
            this.startTimeColumn.setShowSeconds(true);
            this.courseAreaColumn.setShowDetails(RaceListDataUtil.hasCourseAreas(data));
            this.courseColumn.setShowDetails(RaceListDataUtil.hasCourses(data));
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setShowDetails(hasWind);
            this.windDirectionColumn.setShowDetails(hasWind);
            columnSet.updateColumnVisibilities();
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
            
            columnSet.addColumn(regattaNameColumn);
            columnSet.addColumn(windSpeedColumn);
            columnSet.addColumn(windDirectionColumn);
            columnSet.addColumn(courseAreaColumn);
            columnSet.addColumn(courseColumn);
            columnSet.addColumn(fleetNameColumn);
        }

        @Override
        public boolean hasWind() {
            return false;
        }

        @Override
        public boolean hasVideos() {
            return false;
        }

        @Override
        public boolean hasAudios() {
            return false;
        }
    }
}
