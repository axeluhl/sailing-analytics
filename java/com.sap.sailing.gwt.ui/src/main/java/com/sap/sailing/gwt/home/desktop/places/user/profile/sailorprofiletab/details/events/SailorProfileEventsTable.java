package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events;

import java.util.Arrays;
import java.util.HashSet;

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
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedRegattaDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileDesktopResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.ShowAndEditSailorProfile;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.celltable.SortedCellTable;

/** This element displays the events a sailor has participated in in the {@link ShowAndEditSailorProfile} view. */
public class SailorProfileEventsTable extends Composite {

    interface MyBinder extends UiBinder<Widget, SailorProfileEventsTable> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);
    private ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);
    private PlaceController placeController;

    @UiField(provided = true)
    final SortedCellTable<ParticipatedRegattaDTO> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField
    SpanElement titleUi;

    private FlagImageResolver flagImageResolver;

    public SailorProfileEventsTable(FlagImageResolver flagImageResolver, PlaceController placeController,
            ParticipatedEventDTO event) {
        this.flagImageResolver = flagImageResolver;
        this.placeController = placeController;
        initWidget(uiBinder.createAndBindUi(this));
        setupTable(event.getParticipatedRegattas());

        titleUi.setInnerText(event.getEventName());
    }

    private void setupTable(Iterable<ParticipatedRegattaDTO> regattas) {
        sailorProfilesTable.addColumn(regattaNameColumn, StringMessages.INSTANCE.regattaName());
        sailorProfilesTable.addColumn(regattaRank, StringMessages.INSTANCE.regattaRank());
        sailorProfilesTable.addColumn(competitorColumn, StringMessages.INSTANCE.competitor());
        sailorProfilesTable.addColumn(clubNameColumn, StringMessages.INSTANCE.name());
        sailorProfilesTable.addColumn(sumPointsColumn, "\u2211");
        sailorProfilesTable.addColumn(navigatorColumn);

        navigatorColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        navigatorColumn.setFieldUpdater(new FieldUpdater<ParticipatedRegattaDTO, String>() {
            @Override
            public void update(int index, ParticipatedRegattaDTO entry, String value) {
                placeController.goTo(getTargetPlace(entry));
            }
        });

        sailorProfilesTable.setPageSize(Util.size(regattas));
        sailorProfilesTable.setList(regattas);

        addButtonStyle(regattaNameColumn);
        addButtonStyle(regattaRank);
        addButtonStyle(competitorColumn);
        addButtonStyle(clubNameColumn);
        addButtonStyle(sumPointsColumn);
        addButtonStyle(navigatorColumn);

        sailorProfilesTable.addCellPreviewHandler(e -> {
            if ("click".equals(e.getNativeEvent().getType())) {
                placeController.goTo(getTargetPlace(e.getValue()));
            }
        });
    }

    private void addButtonStyle(Column<?, ?> col) {
        col.setCellStyleNames(col.getCellStyleNames(null, null) + " "
                + SailorProfileDesktopResources.INSTANCE.css().clickableColumn());
    }

    private RegattaLeaderboardPlace getTargetPlace(ParticipatedRegattaDTO entry) {
        return new RegattaLeaderboardPlace(entry.getEventId(), entry.getRegattaId(),
                new HashSet<>(Arrays.asList(entry.getCompetitorDto().getIdAsString())));
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
            return entry.getCompetitorDto().getName();
        }
    };

    private final Column<ParticipatedRegattaDTO, Number> sumPointsColumn = new Column<ParticipatedRegattaDTO, Number>(
            new NumberCell()) {
        @Override
        public Number getValue(ParticipatedRegattaDTO entry) {
            return entry.getSumPoints();
        }
    };

    private final Column<ParticipatedRegattaDTO, String> navigatorColumn = new NavigatorColumn<ParticipatedRegattaDTO>() {
        @Override
        public String getValue(ParticipatedRegattaDTO object) {
            return "#" + historyMapper.getToken(getTargetPlace(object));
        }
    };
}
