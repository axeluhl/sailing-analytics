package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;

public class RaceList extends AbstractRaceList<LiveRaceDTO> {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;

    private final boolean showRegattaDetails;
    private boolean showFleetDetails;
    private boolean showCourseAreaDetails;
    private boolean showCourseDetails;
    private boolean showWindDetails;
    
    private final SortableRaceListColumn<?> regattaNameColumn = new SortableRaceListColumn<String>(I18N.regatta(),
            new TextCell(), new InvertibleComparatorWrapper<LiveRaceDTO, String>(new NaturalComparator(false)) {
                @Override
                protected String getComparisonValue(LiveRaceDTO object) {
                    return object.getRegattaDisplayName();
                }
            }) {
        @Override
        public String getHeaderStyle() {
            return CSS.raceslist_head_item();
        }

        @Override
        public String getColumnStyle() {
            return getStyleNamesString(CSS.race_item(), CSS.race_itemname());
        }

        @Override
        public String getValue(LiveRaceDTO object) {
            return object.getRegattaDisplayName();
        }
    };
    private final SortableRaceListColumn<?> flagsColumn = new RaceListColumn<FlagStateDTO>(I18N.flags(),
            new FlagsCell()) {
        @Override
        public String getHeaderStyle() {
            return CSS.raceslist_head_item();
        }

        @Override
        public String getColumnStyle() {
            return CSS.race_item();
        }

        @Override
        public FlagStateDTO getValue(LiveRaceDTO object) {
            return object.getFlagState();
        }
    };
    private final SortableRaceListColumn<?> windSpeedColumn = new SortableRaceListColumn<String>(I18N.wind(),
            new TextCell(), new InvertibleComparatorWrapper<LiveRaceDTO, Double>(
                    new NullSafeComparableComparator<Double>()) {
                @Override
                protected Double getComparisonValue(LiveRaceDTO object) {
                    return object.getWind() != null ? object.getWind().getTrueWindSpeedInKnots() : null;
                }
            }) {
        @Override
        public String getHeaderStyle() {
            return CSS.raceslist_head_item();
        }

        @Override
        public String getColumnStyle() {
            return CSS.race_item();
        }

        @Override
        public String getValue(LiveRaceDTO object) {
            return object.getWind() != null ? I18N.knotsValue(object.getWind().getTrueWindSpeedInKnots()) : null;
        }
    };
    private final SortableRaceListColumn<?> windDirectionColumn = new SortableRaceListColumn<SimpleWindDTO>(
            I18N.from(), new WindDirectionCell(), new InvertibleComparatorWrapper<LiveRaceDTO, Double>(
                    new NullSafeComparableComparator<Double>()) {
                @Override
                protected Double getComparisonValue(LiveRaceDTO object) {
                    return object.getWind() != null ? object.getWind().getTrueWindFromDeg() : null;
                }
            }) {
        @Override
        public String getHeaderStyle() {
            return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.hideonsmall());
        }

        @Override
        public String getColumnStyle() {
            return getStyleNamesString(CSS.race_item(), MEDIA_CSS.hideonsmall());
        }

        @Override
        public SimpleWindDTO getValue(LiveRaceDTO object) {
            return object.getWind();
        }
    };
    private final SortableRaceListColumn<?> raceViewStateColumn = new SortableRaceListColumn<LiveRaceDTO>(
            I18N.status(), new RaceViewStateCell(), new InvertibleComparatorWrapper<LiveRaceDTO, RaceViewState>(
                    new NullSafeComparableComparator<RaceViewState>()) {
                @Override
                protected RaceViewState getComparisonValue(LiveRaceDTO object) {
                    return object.getViewState();
                }
            }) {
        @Override
        public String getHeaderStyle() {
            return CSS.raceslist_head_item();
        }

        @Override
        public String getColumnStyle() {
            return CSS.race_item();
        }

        @Override
        public LiveRaceDTO getValue(LiveRaceDTO object) {
            return object;
        }
    };

    public RaceList(EventView.Presenter presenter, boolean showRegattaDetails) {
        super(presenter);
        this.showRegattaDetails = showRegattaDetails;
    }

    public void setListData(LiveRacesDTO data) {
        this.showFleetDetails = data.hasFleets();
        this.showCourseAreaDetails = data.hasCourseAreas();
        this.showCourseDetails = data.hasCourses();
        this.showWindDetails = data.hasWind();
        setTableData(data.getRaces());
    }

    @Override
    protected void initTableColumns() {
        if (showFleetDetails) {
            add(fleetCornerColumn);
        }
        if (showRegattaDetails) {
            add(regattaNameColumn);
        }
        add(raceNameColumn);
        if (showFleetDetails) {
            add(fleetNameColumn);
        }
        add(startTimeColumn);
        add(flagsColumn);
        if(showWindDetails) {
            add(windSpeedColumn);
            add(windDirectionColumn);
        }
        if (showCourseAreaDetails) {
            add(courseAreaColumn);
        }
        if(showCourseDetails) {
            add(courseColumn);
        }
        add(raceViewStateColumn);
        add(raceViewerButtonColumn);
    }
}
