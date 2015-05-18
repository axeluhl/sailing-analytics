package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sailing.gwt.ui.regattaoverview.FlagsMeaningExplanator;
import com.sap.sailing.gwt.ui.regattaoverview.SailingFlagsBuilder;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.LiveRaceState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.theme.client.component.celltable.CleanCellTableResources;
import com.sap.sse.gwt.theme.client.component.celltable.StyledHeaderOrFooterBuilder;

public abstract class AbstractRaceList<T extends RaceMetadataDTO> extends Composite {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final CellTemplate TEMPLATE = GWT.create(CellTemplate.class);

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{1}\" class=\"{0}\"></div>")
        SafeHtml fleetCorner(String styleNames, SafeStyles color);

        @Template("<a href=\"{2}\" class=\"{0}\" target=\"_blank\">{1}</a>")
        SafeHtml watchNowButton(String styleNames, String text, String link);

        @Template("<div>{3}</div><div class=\"{0}\"><div style=\"{2}\" class=\"{1}\"></div></div>")
        SafeHtml raceProgress(String styleNamesBar, String styleNamesProgress, SafeStyles width, String text);

        @Template("<img style=\"{0}\" src=\"images/home/windkompass_nord.svg\"/>")
        SafeHtml windDirection(SafeStyles rotation);
    }

    private final SimplePanel cellTableContainer = new SimplePanel();
    private final EventView.Presenter presenter;
    
    private SortedCellTable<T> cellTable;
    
    protected AbstractRaceList(EventView.Presenter presenter) {
        CSS.ensureInjected();
        this.presenter = presenter;
        this.initWidget(cellTableContainer);
    }

    protected void setTableData(List<T> data) {
        this.cellTable = new SortedCellTable<T>(data.size(), CleanCellTableResources.INSTANCE);
        this.cellTableContainer.setWidget(this.cellTable);
        this.initTableStyle();
        this.initTableColumns();
        this.cellTable.setList(data);
        this.cellTable.sort();
    }

    private void initTableStyle() {
        this.cellTable.addStyleName(CSS.raceslist());
        this.cellTable.setHeaderBuilder(new StyledHeaderOrFooterBuilder<T>(cellTable, false, CSS.raceslist_head()));
        this.cellTable.setRowStyles(new RowStyles<T>() {
            @Override
            public String getStyleNames(T row, int rowIndex) {
                return CSS.race();
            }
        });
    }
    
    protected abstract void initTableColumns();

    protected void addFleetCornerColumn() {
        add(new RaceListColumn<FleetMetadataDTO>("", new FleetCornerCell()) {
            @Override
            public String getColumnStyle() {
                return CSS.race_fleetcorner();
            }

            @Override
            public FleetMetadataDTO getValue(T object) {
                return object.getFleet();
            }
        });
    }

    protected void addRaceNameColumn() {
        add(new SortableRaceListColumn<String>(I18N.race(), new TextCell()) {
            @Override
            public InvertibleComparator<T> getComparator() {
                return new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
                    @Override
                    protected String getComparisonValue(T object) {
                        return object.getRaceName();
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
            public String getValue(T object) {
                return object.getRaceName();
            }
        });
    }

    protected void addFleetNameColumn() {
        add(new SortableRaceListColumn<String>(I18N.fleet(), new TextCell()) {
            @Override
            public InvertibleComparator<T> getComparator() {
                return new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
                    @Override
                    protected String getComparisonValue(T object) {
                        return object.getFleet() != null ? object.getFleet().getFleetName() : null;
                    }
                };
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
            public String getValue(T object) {
                return object.getFleet() != null ? object.getFleet().getFleetName() : null;
            }
        });
    }
    
    protected void addStartTimeColumn() {
        add(new SortableRaceListColumn<Date>(I18N.start(), new DateCell(
                DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE))) {
            @Override
            public InvertibleComparator<T> getComparator() {
                return new InvertibleComparatorWrapper<T, Date>(new NullSafeComparableComparator<Date>()) {
                    @Override
                    protected Date getComparisonValue(T object) {
                        return object.getStart();
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
            public Date getValue(T object) {
                return object.getStart();
            }
        });
    }

    protected void addCourseAreaColumn() {
        add(new SortableRaceListColumn<String>(I18N.courseArea(), new TextCell()) {
            @Override
            public InvertibleComparator<T> getComparator() {
                return new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
                    @Override
                    protected String getComparisonValue(T object) {
                        return object.getCourseArea();
                    }
                };
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
            public String getValue(T object) {
                return object.getCourseArea();
            }
        });
    }

    protected void addCourseColumn() {
        add(new SortableRaceListColumn<String>(I18N.course(), new TextCell()) {
            @Override
            public InvertibleComparator<T> getComparator() {
                return new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
                    @Override
                    protected String getComparisonValue(T object) {
                        return object.getCourse();
                    }
                };
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
            public String getValue(T object) {
                return object.getCourse();
            }
        });
    }

    protected void addRaceViewerButtonCell() {
        add(new RaceListColumn<T>("", new RaceViewerButtonCell()) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), CSS.raceslist_head_itembutton());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemright());
            }

            @Override
            public T getValue(T object) {
                return object;
            }
        });
    }

    protected void add(SortableRaceListColumn<?> column) {
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

    protected abstract class SortableRaceListColumn<C> extends SortableColumn<T, C> {
        private final String headerText;

        protected SortableRaceListColumn(String headerText, Cell<C> cell) {
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

    protected abstract class RaceListColumn<C> extends SortableRaceListColumn<C> {
        protected RaceListColumn(String headerText, Cell<C> cell) {
            super(headerText, cell);
        }

        @Override
        public final InvertibleComparator<T> getComparator() {
            return null;
        }
    }

    private class FleetCornerCell extends AbstractCell<FleetMetadataDTO> {
        @Override
        public void render(Context context, FleetMetadataDTO value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(TEMPLATE.fleetCorner(CSS.race_fleetcorner_icon(),
                        SafeStylesUtils.fromTrustedNameAndValue("border-top-color", value.getFleetColor())));
            }
        }
    }

    protected class FlagsCell extends AbstractCell<FlagStateDTO> {
        @Override
        public void render(Context context, FlagStateDTO value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(SailingFlagsBuilder.render(value, 0.363636, FlagsMeaningExplanator.getFlagsMeaning(I18N,
                        value.getLastUpperFlag(), value.getLastLowerFlag(), value.isLastFlagsAreDisplayed())));
            }
        }
    }

    protected class WindDirectionCell extends AbstractCell<SimpleWindDTO> {
        @Override
        public void render(Context context, SimpleWindDTO value, SafeHtmlBuilder sb) {
            if (value != null) {
                SafeStylesBuilder safeStyles = new SafeStylesBuilder();
                safeStyles.trustedNameAndValue("transform", "rotate(" + value.getTrueWindFromDeg() + "deg)");
                safeStyles.width(2.5, Unit.EM);
                safeStyles.height(2.5, Unit.EM);
                safeStyles.trustedNameAndValue("margin", "-0.75em auto");
                sb.append(TEMPLATE.windDirection(safeStyles.toSafeStyles()));
            }
        }
    }

    protected class RaceProgressCell extends AbstractCell<LiveRaceDTO> {
        @Override
        public void render(Context context, LiveRaceDTO value, SafeHtmlBuilder sb) {
            LiveRaceState state = value.getState();
            RaceProgressDTO progress = value.getProgress();
            if (state != LiveRaceState.LIVE) {
                sb.appendEscaped(state.name());
            } else if (progress != null) {
                SafeStyles width = SafeStylesUtils.forWidth(progress.getPercentageProgress(), Unit.PCT);
                String text = I18N.currentOfTotalLegs(progress.getCurrentLeg(), progress.getTotalLegs());
                sb.append(TEMPLATE.raceProgress(CSS.race_itemstatus_progressbar(),
                        CSS.race_itemstatus_progressbar_progress(), width, text));
            }
        }
    }

    private class RaceViewerButtonCell extends AbstractCell<T> {
        private final MainCss css = SharedResources.INSTANCE.mainCss();
        private final String watchNowStyle = getButtonStyleNames(css.buttonred());
        private final String analyseRaceStyle = getButtonStyleNames(css.buttonprimary());

        @Override
        public void render(Context context, T data, SafeHtmlBuilder sb) {
            String styleNames = data.getState() == LiveRaceState.FINISHED ? analyseRaceStyle : watchNowStyle;
            String text = data.getState() == LiveRaceState.FINISHED ? I18N.analyseRace() : TextMessages.INSTANCE.watchNow();
            String raceViewerURL = presenter.getRaceViewerURL(data.getID());
            sb.append(TEMPLATE.watchNowButton(styleNames, text, raceViewerURL));
        }

        private final String getButtonStyleNames(String buttonColor) {
            return Util.join(" ", css.button(), css.buttonstrong(), buttonColor, css.buttonarrowrightwhite());
        }
    }
}
