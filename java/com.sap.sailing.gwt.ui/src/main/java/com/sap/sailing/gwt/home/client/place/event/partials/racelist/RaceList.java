package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;

public class RaceList extends AbstractRaceList<LiveRaceDTO> {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;

    public RaceList(EventView.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected void initTableColumns() {
        addFleetCornerColumn();

        add(new SortableRaceListColumn<String>(I18N.regatta(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return new InvertibleComparatorWrapper<LiveRaceDTO, String>(new NaturalComparator(false)) {
                    @Override
                    protected String getComparisonValue(LiveRaceDTO object) {
                        return object.getRegattaName();
                    }
                };
            }

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
                return object.getRegattaName();
            }
        });

        addRaceNameColumn();
        addFleetNameColumn();
        addStartTimeColumn();

        add(new RaceListColumn<FlagStateDTO>(I18N.flags(), new FlagsCell()) {
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
        });

        add(new SortableRaceListColumn<String>(I18N.wind(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return new InvertibleComparatorWrapper<LiveRaceDTO, Double>(new NullSafeComparableComparator<Double>()) {
                    @Override
                    protected Double getComparisonValue(LiveRaceDTO object) {
                        return object.getWind() != null ? object.getWind().getTrueWindSpeedInKnots() : null;
                    }
                };
            }

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
        });

        add(new RaceListColumn<SimpleWindDTO>(I18N.from(), new WindDirectionCell()) {
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
        });

        addCourseAreaColumn();
        addCourseColumn();

        add(new SortableRaceListColumn<LiveRaceDTO>(I18N.status(), new RaceProgressCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return new InvertibleComparatorWrapper<LiveRaceDTO, Double>(new NullSafeComparableComparator<Double>()) {
                    @Override
                    protected Double getComparisonValue(LiveRaceDTO object) {
                        return object.getProgress().getPercentageProgress();
                    }
                };
            }

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
        });

        addRaceViewerButtonCell();
    }
}
