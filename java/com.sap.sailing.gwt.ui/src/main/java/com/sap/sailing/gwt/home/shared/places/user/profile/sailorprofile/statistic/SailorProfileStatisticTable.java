package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.statistic;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events.CompetitorWithoutClubnameItemDescription;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events.NavigatorColumn;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.common.Util.Pair;

public class SailorProfileStatisticTable extends Composite {

    interface MyBinder extends UiBinder<Widget, SailorProfileStatisticTable> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private PlaceController placeController;

    @UiField(provided = true)
    final SortedCellTable<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField
    SpanElement titleUi;

    private FlagImageResolver flagImageResolver;

    private SailorProfileNumericStatisticType type;

    private StringMessages stringMessages;

    public SailorProfileStatisticTable(FlagImageResolver flagImageResolver, SailorProfileNumericStatisticType type,
            StringMessages stringMessages, PlaceController placeController) {
        this.flagImageResolver = flagImageResolver;
        this.type = type;
        this.stringMessages = stringMessages;
        this.placeController = placeController;
        initWidget(uiBinder.createAndBindUi(this));
        setupTable();
        titleUi.setInnerText(SailorProfileNumericStatisticTypeFormater.getDisplayName(type, stringMessages));
    }

    public void setData(List<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> data) {
        sailorProfilesTable.setPageSize(data.size());
        sailorProfilesTable.setList(data);
    }

    private void setupTable() {
        sailorProfilesTable.addColumn(actualValueColumn, StringMessages.INSTANCE.regattaName());
        // sailorProfilesTable.addColumn(regattaRank, StringMessages.INSTANCE.regattaRank());
        // sailorProfilesTable.addColumn(sumPointsColumn, "\u2211");
        sailorProfilesTable.addColumn(competitorColumn, StringMessages.INSTANCE.competitor());
        sailorProfilesTable.addColumn(clubNameColumn, StringMessages.INSTANCE.name());
        // sailorProfilesTable.addColumn(navigatorColumn);

        navigatorColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        navigatorColumn.setFieldUpdater(new FieldUpdater<ParticipatedRegattaDTO, String>() {
            @Override
            public void update(int index, ParticipatedRegattaDTO entry, String value) {
                placeController.goTo(new RegattaLeaderboardPlace(entry.getEventId(), entry.getRegattaId()));
            }
        });
    }

    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> actualValueColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return SailorProfileNumericStatisticTypeFormater.format(type, entry.getB().getValue(), stringMessages);
        }
    };
    private final Column<ParticipatedRegattaDTO, Number> regattaRank = new Column<ParticipatedRegattaDTO, Number>(
            new NumberCell()) {
        @Override
        public Number getValue(ParticipatedRegattaDTO entry) {
            return entry.getRegattaRank();
        }
    };
    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, SimpleCompetitorWithIdDTO> competitorColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, SimpleCompetitorWithIdDTO>(
            new AbstractCell<SimpleCompetitorWithIdDTO>() {
                @Override
                public void render(Context context, SimpleCompetitorWithIdDTO value, SafeHtmlBuilder sb) {
                    sb.appendHtmlConstant(new CompetitorWithoutClubnameItemDescription(value, flagImageResolver)
                            .getElement().getInnerHTML());
                }
            }) {
        @Override
        public SimpleCompetitorWithIdDTO getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> object) {
            return object.getA();
        }
    };
    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> clubNameColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return entry.getA().getName();
        }
    };
    private final Column<ParticipatedRegattaDTO, Number> sumPointsColumn = new Column<ParticipatedRegattaDTO, Number>(
            new NumberCell()) {
        @Override
        public Number getValue(ParticipatedRegattaDTO entry) {
            return entry.getSumPoints();
        }
    };

    private final Column<ParticipatedRegattaDTO, String> navigatorColumn = new NavigatorColumn<ParticipatedRegattaDTO>();
}
