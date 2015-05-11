package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRacesDTO;


public class RaceList extends Composite implements RefreshableWidget<LiveRacesDTO> {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;

    private static final CustomResources RESOURCES = GWT.create(CustomResources.class);
    private final CellTable<LiveRaceDTO> cellTable = new CellTable<LiveRaceDTO>(0, RESOURCES);
    private final DateTimeFormat startTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE);

    interface CustomResources extends CellTable.Resources {
        @Override
        @Source("RaceList.css")
        public Style cellTableStyle();
    }

    public RaceList() {
        CSS.ensureInjected();
        cellTable.addStyleName(CSS.raceslist());
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
        TextColumn<LiveRaceDTO> fleetCornerColumn = new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "";
            }
        };
        fleetCornerColumn.setCellStyleNames(CSS.race_fleetcorner());
        this.cellTable.addColumn(fleetCornerColumn, getStyledHeader(""));

        this.cellTable.addColumn(getStyledColumn(new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getRegattaName();
            }
        }, CSS.race_itemname()), getStyledHeader(I18N.regatta()));

        this.cellTable.addColumn(getStyledColumn(new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getRaceName();
            }
        }, CSS.race_itemname()), getStyledHeader(I18N.race()));

        add(I18N.fleet(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getFleetName();
            }
        }, MEDIA_CSS.showonlarge());

        add(I18N.start(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return object.getStart() == null ? "-" : startTimeFormat.format(object.getStart());
            }
        });

        add("TODO FLAG", new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO FLAG";
            }
        });

        add(I18N.wind(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO WIND";
            }
        });

        add(I18N.from(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO FROM";
            }
        }, MEDIA_CSS.hideonsmall());

        add("TODO AREA", new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO AREA";
            }
        }, MEDIA_CSS.showonlarge());

        add(I18N.course(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO COURSE";
            }
        }, MEDIA_CSS.showonlarge());

        add(I18N.status(), new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO STATUS";
            }
        });

        this.cellTable.addColumn(getStyledColumn(new TextColumn<LiveRaceDTO>() {
            @Override
            public String getValue(LiveRaceDTO object) {
                return "TODO WATCH NOW";
            }
        }, CSS.race_itemright()), getStyledHeader("", CSS.raceslist_head_itembutton()));

    }

    private Header<?> getStyledHeader(String headerText, String... additionalStyles) {
        Header<String> header = new TextHeader(headerText);
        header.setHeaderStyleNames(createStyleNameString(CSS.raceslist_head_item(), additionalStyles));
        return header;
    }

    private <C> Column<LiveRaceDTO, C> getStyledColumn(Column<LiveRaceDTO, C> column, String... additionalStyles) {
        column.setCellStyleNames(createStyleNameString(CSS.race_item(), additionalStyles));
        return column;
    }

    private void add(String headerText, Column<LiveRaceDTO, ?> column, String... mediaStyle) {
        this.cellTable.addColumn(getStyledColumn(column, mediaStyle), getStyledHeader(headerText, mediaStyle));
    }

    private String createStyleNameString(String primaryStyle, String... additionalStyles) {
        assert primaryStyle != null : "primaryStyle parameter cannot be null";
        StringBuilder styleName = new StringBuilder(primaryStyle);
        for (int i = 0; i < additionalStyles.length; i++) {
            styleName.append(" ").append(additionalStyles[i]);
        }
        return styleName.toString();
    }

    @Override
    public void setData(LiveRacesDTO data, long nextUpdate, int updateNo) {
        this.cellTable.setRowData(data.getRaces());
        this.cellTable.setPageSize(data.getRaces().size());
    }

}
