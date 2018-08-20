package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
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
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.common.Util;

public class SailorProfileEventsTable extends Composite {

    interface MyBinder extends UiBinder<Widget, SailorProfileEventsTable> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private PlaceController placeController;

    @UiField(provided = true)
    final SortedCellTable<ParticipatedRegattaDTO> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField
    SpanElement titleUi;

    private FlagImageResolver flagImageResolver;

    public SailorProfileEventsTable(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    public void setController(PlaceController placeController) {
        this.placeController = placeController;
        initWidget(uiBinder.createAndBindUi(this));
        setupTable();
    }

    public void setEvent(ParticipatedEventDTO event) {
        titleUi.setInnerText(event.getEventName());
        sailorProfilesTable.setPageSize(Util.size(event.getParticipatedRegattas()));
        sailorProfilesTable.setList(event.getParticipatedRegattas());
    }

    private void setupTable() {
        sailorProfilesTable.addColumn(regattaNameColumn, "Regatta Name");
        sailorProfilesTable.addColumn(regattaRank, "Regatta Rank");
        sailorProfilesTable.addColumn(competitorColumn, "Competitor");
        sailorProfilesTable.addColumn(clubNameColumn, "Club Name");
        sailorProfilesTable.addColumn(sumPointsColumn, "Sub Points");
        sailorProfilesTable.addColumn(showColumn, "");

        showColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        showColumn.setFieldUpdater(new FieldUpdater<ParticipatedRegattaDTO, String>() {
            @Override
            public void update(int index, ParticipatedRegattaDTO entry, String value) {
                placeController.goTo(new RegattaLeaderboardPlace(entry.getEventId(), entry.getRegattaId()));
            }
        });
    }

    private final Column<ParticipatedRegattaDTO, String> regattaNameColumn = new Column<ParticipatedRegattaDTO, String>(
            new TextCell()) {
        @Override
        public String getValue(ParticipatedRegattaDTO entry) {
            return entry.getRegattaName();
        }
    };
    private final Column<ParticipatedRegattaDTO, Number> regattaRank = new Column<ParticipatedRegattaDTO, Number>(
            new NumberCell()) {
        @Override
        public Number getValue(ParticipatedRegattaDTO entry) {
            return entry.getRegattaRank();
        }
    };
    private final Column<ParticipatedRegattaDTO, ParticipatedRegattaDTO> competitorColumn = new Column<ParticipatedRegattaDTO, ParticipatedRegattaDTO>(
            new AbstractCell<ParticipatedRegattaDTO>() {
                @Override
                public void render(Context context, ParticipatedRegattaDTO value, SafeHtmlBuilder sb) {
                    sb.appendHtmlConstant(
                            new CompetitorWithoutClubnameItemDescription(value.getCompetitorDto(), flagImageResolver)
                                    .getElement().getInnerHTML());
                }
            }) {
        @Override
        public ParticipatedRegattaDTO getValue(ParticipatedRegattaDTO object) {
            return object;
        }
    };
    private final Column<ParticipatedRegattaDTO, String> clubNameColumn = new Column<ParticipatedRegattaDTO, String>(
            new TextCell()) {
        @Override
        public String getValue(ParticipatedRegattaDTO entry) {
            return entry.getClubName();
        }
    };
    private final Column<ParticipatedRegattaDTO, Number> sumPointsColumn = new Column<ParticipatedRegattaDTO, Number>(
            new NumberCell()) {
        @Override
        public Number getValue(ParticipatedRegattaDTO entry) {
            return entry.getSumPoints();
        }
    };
    private final Column<ParticipatedRegattaDTO, String> showColumn = new Column<ParticipatedRegattaDTO, String>(
            new ButtonCell()) {
        @Override
        public String getValue(ParticipatedRegattaDTO entry) {
            return ">";
        }
    };
}
