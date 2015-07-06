package com.sap.sailing.gwt.home.client.place.event.partials.raceListLive;

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
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;

public class RacesListLive extends Composite implements RefreshableWidget<SortedSetResult<LiveRaceDTO>> {

    private final RaceListLiveRaces raceList;
    
    public RacesListLive(EventView.Presenter presenter, boolean showRegattaDetails) {
        raceList = new RaceListLiveRaces(presenter, showRegattaDetails);
        initWidget(new RaceListContainer<LiveRaceDTO>(StringMessages.INSTANCE.liveNow(), raceList));
        getElement().getStyle().setDisplay(Display.NONE);
    }

    @Override
    public void setData(SortedSetResult<LiveRaceDTO> data, long nextUpdate, int updateNo) {
        this.setListData(data.getValues());
    }
    
    public void setListData(Collection<LiveRaceDTO> data) {
        if(data == null || data.isEmpty()) {
            getElement().getStyle().setDisplay(Display.NONE);
        } else {
            getElement().getStyle().clearDisplay();
            raceList.setListData(data);
        }
    }
    
    private class RaceListLiveRaces extends AbstractRaceList<LiveRaceDTO> {
        private final SortableRaceListColumn<LiveRaceDTO, ?> regattaNameColumn = RaceListColumnFactory.getRegattaNameColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> flagsColumn = RaceListColumnFactory.getFlagsColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseAreaColumn = RaceListColumnFactory.getCourseAreaColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> courseColumn = RaceListColumnFactory.getCourseColumn();
        private final SortableRaceListColumn<LiveRaceDTO, ?> raceViewStateColumn = RaceListColumnFactory.getRaceViewStateColumn();

        public RaceListLiveRaces(EventView.Presenter presenter, boolean showRegattaDetails) {
            super(presenter);
            this.regattaNameColumn.setShowDetails(showRegattaDetails);
        }

        public void setListData(Collection<LiveRaceDTO> data) {
            boolean hasFleets = RaceListDataUtil.hasFleets(data);
            this.fleetCornerColumn.setShowDetails(hasFleets);
            this.fleetNameColumn.setShowDetails(hasFleets);
            this.courseAreaColumn.setShowDetails(RaceListDataUtil.hasCourseAreas(data));
            this.courseColumn.setShowDetails(RaceListDataUtil.hasCourses(data));
            boolean hasWind = RaceListDataUtil.hasWind(data);
            this.windSpeedColumn.setShowDetails(hasWind);
            this.windDirectionColumn.setShowDetails(hasWind);
            setTableData(data);
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
