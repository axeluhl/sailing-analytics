package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sailing.gwt.ui.regattaoverview.FlagsMeaningExplanator;
import com.sap.sailing.gwt.ui.regattaoverview.SailingFlagsBuilder;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.theme.client.component.celltable.CleanCellTableResources;
import com.sap.sse.gwt.theme.client.component.celltable.StyledHeaderOrFooterBuilder;

public class RaceList extends Composite implements RefreshableWidget<LiveRacesDTO> {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MainCss MAIN_CSS = SharedResources.INSTANCE.mainCss();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final TextMessages I18N_UBI = TextMessages.INSTANCE;
    private static final CellTemplate TEMPLATE = GWT.create(CellTemplate.class);

    private final SortedCellTable<LiveRaceDTO> cellTable = new SortedCellTable<LiveRaceDTO>(0,
            CleanCellTableResources.INSTANCE);
    private final DateTimeFormat startTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE);

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{1}\" class=\"{0}\"></div>")
        SafeHtml fleetCorner(String styleNames, SafeStyles color);

        @Template("<a class=\"{0}\">{1}</a>")
        SafeHtml watchNowButton(String styleNames, String text);

        @Template("<div>{3}</div><div class=\"{0}\"><div style=\"{2}\" class=\"{1}\"></div></div>")
        SafeHtml raceProgress(String styleNamesBar, String styleNamesProgress, SafeStyles width, String text);
    }

    public RaceList() {
        CSS.ensureInjected();
        this.cellTable.addStyleName(CSS.raceslist());
        this.cellTable.setHeaderBuilder(new StyledHeaderOrFooterBuilder<LiveRaceDTO>(cellTable, false, CSS
                .raceslist_head()));
        this.cellTable.setRowStyles(new RowStyles<LiveRaceDTO>() {
            @Override
            public String getStyleNames(LiveRaceDTO row, int rowIndex) {
                return CSS.race();
            }
        });
        initColumns();
        initWidget(cellTable);
    }

    private void initColumns() {
        add(new RaceListColumn<String>("", new FleetCornerCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getColumnStyle() {
                return CSS.race_fleetcorner();
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getFleet() != null ? object.getFleet().getFleetColor() : "";
            }
        });

        add(new RaceListColumn<String>(I18N.regatta(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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

        add(new RaceListColumn<String>(I18N.race(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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
                return object.getRaceName();
            }
        });

        add(new RaceListColumn<String>(I18N.fleet(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getFleet() != null ? object.getFleet().getFleetName() : "-";
            }
        });

        add(new RaceListColumn<String>(I18N.start(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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
                return object.getStart() == null ? "-" : startTimeFormat.format(object.getStart());
            }
        });

        add(new RaceListColumn<FlagStateDTO>(I18N.flags(), new FlagsCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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
            public FlagStateDTO getValue(LiveRaceDTO object) {
                return object.getFlagState();
            }
        });

        add(new RaceListColumn<Number>(I18N.wind(), new NumberCell(NumberFormatterFactory.getDecimalFormat(1))) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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
            public Number getValue(LiveRaceDTO object) {
                return object.getWind() != null ? object.getWind().getTrueWindSpeedInKnots() : null;
            }
        });

        add(new RaceListColumn<Number>(I18N.from(), new NumberCell(NumberFormatterFactory.getDecimalFormat(0))) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public Number getValue(LiveRaceDTO object) {
                return object.getWind() != null ? object.getWind().getTrueWindFromDeg() : null;
            }
        });

        add(new RaceListColumn<String>(I18N.courseArea(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getCourseArea();
            }
        });

        add(new RaceListColumn<String>(I18N.course(), new TextCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return String.valueOf(object.getCourse());
            }
        });

        add(new RaceListColumn<RaceProgressDTO>(I18N.status(), new RaceProgressCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
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
            public RaceProgressDTO getValue(LiveRaceDTO object) {
                return object.getProgress();
            }
        });

        RaceListColumn<String> watchNowButtonColumn = new RaceListColumn<String>("", new WatchNowButtonCell()) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), CSS.raceslist_head_itembutton());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemright());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return I18N_UBI.watchNow();
            }
        };
        watchNowButtonColumn.setFieldUpdater(new FieldUpdater<LiveRaceDTO, String>() {
            @Override
            public void update(int index, LiveRaceDTO object, String value) {
                Window.alert("Watch now: " + object.getRegattaName() + " " + object.getRaceName()); // TODO
            }
        });
        add(watchNowButtonColumn);
    }

    private void add(RaceListColumn<?> column) {
        if (column.getColumnStyle() != null) {
            column.setCellStyleNames(column.getColumnStyle());
        }
        Header<?> header = column.getHeader();
        if (header != null && column.getHeaderStyle() != null) {
            header.setHeaderStyleNames(column.getHeaderStyle());
        }
        boolean ascending = column.getPreferredSortingOrder().isAscending();
        this.cellTable.addColumn(column, header, column.getComparator(), ascending);
    }

    @Override
    public void setData(LiveRacesDTO data, long nextUpdate, int updateNo) {
        this.cellTable.setRowData(data.getRaces());
        this.cellTable.setPageSize(data.getRaces().size());
    }

    private abstract static class RaceListColumn<C> extends SortableColumn<LiveRaceDTO, C> {
        private final String headerText;

        protected RaceListColumn(String headerText, Cell<C> cell) {
            super(cell, SortingOrder.ASCENDING);
            this.headerText = headerText;
        }

        @Override
        public final Header<?> getHeader() {
            return new TextHeader(headerText);
        }

        protected final String getStyleNamesString(String... styleNames) {
            return Util.join(" ", styleNames);
        }
    }

    private static class FleetCornerCell extends AbstractCell<String> {
        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.append(TEMPLATE.fleetCorner(CSS.race_fleetcorner_icon(),
                    SafeStylesUtils.fromTrustedNameAndValue("border-top-color", value)));
        }
    }

    private static class FlagsCell extends AbstractCell<FlagStateDTO> {
        @Override
        public void render(Context context, FlagStateDTO value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(SailingFlagsBuilder.render(value, 0.363636, FlagsMeaningExplanator.getFlagsMeaning(I18N,
                        value.getLastUpperFlag(), value.getLastLowerFlag(), value.isLastFlagsAreDisplayed())));
            }
        }
    }

    private static class RaceProgressCell extends AbstractCell<RaceProgressDTO> {
        @Override
        public void render(Context context, RaceProgressDTO value, SafeHtmlBuilder sb) {
            if (value != null) {
                SafeStyles width = SafeStylesUtils.forWidth(value.getPercentageProgress(), Unit.PCT);
                String text = I18N.currentOfTotalLegs(value.getCurrentLeg(), value.getTotalLegs());
                sb.append(TEMPLATE.raceProgress(CSS.race_itemstatus_progressbar(),
                        CSS.race_itemstatus_progressbar_progress(), width, text));
            }
        }
    }

    private static class WatchNowButtonCell extends ButtonCell {
        @Override
        public void render(Context context, String data, SafeHtmlBuilder sb) {
            String styleNames = Util.join(" ", MAIN_CSS.button(), MAIN_CSS.buttonstrong(), MAIN_CSS.buttonred(),
                    MAIN_CSS.buttonarrowrightwhite());
            sb.append(TEMPLATE.watchNowButton(styleNames, data));
        }
    }

}
