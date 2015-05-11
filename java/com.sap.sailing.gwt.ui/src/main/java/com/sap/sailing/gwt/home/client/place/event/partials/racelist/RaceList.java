package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.theme.client.component.celltable.CleanCellTableResources;
import com.sap.sse.gwt.theme.client.component.celltable.StyledHeaderOrFooterBuilder;


public class RaceList extends Composite implements RefreshableWidget<LiveRacesDTO> {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MainCss MAIN_CSS = SharedResources.INSTANCE.mainCss();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final CustomTemplate TEMPLATE = GWT.create(CustomTemplate.class);

    private final SortedCellTable<LiveRaceDTO> cellTable = new SortedCellTable<LiveRaceDTO>(0,
            CleanCellTableResources.INSTANCE);
    private final DateTimeFormat startTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE);

    interface CustomTemplate extends SafeHtmlTemplates {
        @Template("<div style=\"{1}\" class=\"{0}\"></div>")
        SafeHtml fleetCorner(String styleNames, SafeStyles color);

        @Template("<a class=\"{0}\">{1}</a>")
        SafeHtml watchNowButton(String styleNames, String text);
    }

    public RaceList() {
        CSS.ensureInjected();
        cellTable.addStyleName(CSS.raceslist());
        cellTable
                .setHeaderBuilder(new StyledHeaderOrFooterBuilder<LiveRaceDTO>(cellTable, false, CSS.raceslist_head()));
        cellTable.setRowStyles(new RowStyles<LiveRaceDTO>() {
            @Override
            public String getStyleNames(LiveRaceDTO row, int rowIndex) {
                return CSS.race();
            }
        });
        initColumns();
        initWidget(cellTable);
    }

    private void initColumns() {
        add(new SortableColumn<LiveRaceDTO, SafeHtml>(new SafeHtmlCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("");
            }

            @Override
            public String getColumnStyle() {
                return CSS.race_fleetcorner();
            }

            @Override
            public SafeHtml getValue(LiveRaceDTO object) {
                String color = "";
                if (object.getFleet() != null) {
                    color = object.getFleet().getFleetColor();
                }
                return TEMPLATE.fleetCorner(CSS.race_fleetcorner_icon(),
                        SafeStylesUtils.fromTrustedNameAndValue("border-top-color", color));
            }
        });
        
        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.regatta());
            }

            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), CSS.race_itemname());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getRegattaName();
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.race());
            }

            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), CSS.race_itemname());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getRaceName();
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.fleet());
            }

            @Override
            public String getHeaderStyle() {
                return Util.join(" ", CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getFleet() != null ? object.getFleet().getFleetName() : "-";
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.start());
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

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("TODO FLAG");
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
                FlagStateDTO flagState = object.getFlagState();
                if (flagState != null) {
                    return flagState.getLastLowerFlag().name() + " " + flagState.getLastUpperFlag().name();
                }
                return "-";
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("TODO WIND");
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
                if (object.getWind() != null) {
                    return String.valueOf(object.getWind().getTrueWindSpeedInKnots());
                }
                return "-";
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("TODO FROM");
            }

            @Override
            public String getHeaderStyle() {
                return Util.join(" ", CSS.raceslist_head_item(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                if (object.getWind() != null) {
                    return String.valueOf(object.getWind().getTrueWindFromDeg());
                }
                return "-";
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("TODO AREA");
            }

            @Override
            public String getHeaderStyle() {
                return Util.join(" ", CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getCourseArea();
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.course());
            }

            @Override
            public String getHeaderStyle() {
                return Util.join(" ", CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), MEDIA_CSS.showonlarge());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return String.valueOf(object.getCourse());
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new TextCell(), SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader(I18N.status());
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
                return "TODO STATUS";
            }
        });

        add(new SortableColumn<LiveRaceDTO, String>(new ButtonCell() {
            @Override
            public void render(Context context, String data, SafeHtmlBuilder sb) {
                sb.append(TEMPLATE.watchNowButton(
                        Util.join(" ", MAIN_CSS.button(), MAIN_CSS.buttonstrong(), MAIN_CSS.buttonred(),
                                MAIN_CSS.buttonarrowrightwhite()), data));
            }
        }, SortingOrder.ASCENDING) {
            @Override
            public InvertibleComparator<LiveRaceDTO> getComparator() {
                return null;
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("");
            }

            @Override
            public String getHeaderStyle() {
                return Util.join(" ", CSS.raceslist_head_item(), CSS.raceslist_head_itembutton());
            }

            @Override
            public String getColumnStyle() {
                return Util.join(" ", CSS.race_item(), CSS.race_itemright());
            }

            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO WATCH NOW";
            }
        });
    }

    private void add(SortableColumn<LiveRaceDTO, ?> column) {
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

}
