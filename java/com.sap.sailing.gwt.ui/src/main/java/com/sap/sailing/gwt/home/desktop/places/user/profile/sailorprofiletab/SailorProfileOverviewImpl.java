package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.BoatClassImageResolver;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.communication.user.profile.domain.BadgeDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfilePlace;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfileOverviewImpl extends Composite implements SailorProfileOverview {

    interface MyBinder extends UiBinder<Widget, SailorProfileOverviewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);
    private SailingProfileOverviewPresenter presenter;

    @UiField(provided = true)
    final SortedCellTable<SailorProfileEntry> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;

    @Override
    public void setPresenter(SailingProfileOverviewPresenter presenter) {
        this.presenter = presenter;
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        initWidget(uiBinder.createAndBindUi(this));
        setupTable();
    }

    @Override
    public void setProfileList(Collection<SailorProfileEntry> entries) {
        sailorProfilesTable.setPageSize(entries.size());
        sailorProfilesTable.setList(entries);
    }

    private void setupTable() {
        sailorProfilesTable.addColumn(profileNameColumn, "Profile Name");
        sailorProfilesTable.addColumn(badgeColumn, "Badges");
        sailorProfilesTable.addColumn(competitorColumn, "Competitor");
        sailorProfilesTable.addColumn(boatClassColumn, "Boatclass");
        sailorProfilesTable.addColumn(showColumn, "");

        showColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        showColumn.setFieldUpdater(new FieldUpdater<SailorProfileEntry, String>() {
            @Override
            public void update(int index, SailorProfileEntry entry, String value) {
                presenter.getClientFactory().getPlaceController().goTo(new SailorProfilePlace(entry.getKey()));
            }
        });
    }

    private final Column<SailorProfileEntry, String> profileNameColumn = new Column<SailorProfileEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            return entry.getName();
        }
    };
    private final Column<SailorProfileEntry, String> badgeColumn = new Column<SailorProfileEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            if (entry != null && entry.getBadges() != null) {
                return entry.getBadges().stream().map(BadgeDTO::getName).collect(Collectors.joining(", "));
            }
            return "-";
        }
    };
    private final Column<SailorProfileEntry, SailorProfileEntry> boatClassColumn = new Column<SailorProfileEntry, SailorProfileEntry>(
            new AbstractCell<SailorProfileEntry>() {
                @Override
                public void render(Context context, SailorProfileEntry value, SafeHtmlBuilder sb) {
                    for (BoatClassDTO boatclass : value.getBoatclasses()) {
                        sb.appendHtmlConstant(
                                "<div style=\"height: 40px; width: 40px; margin-left: 5px; display: inline-block; background-size: cover; background-image: url('"
                                        + BoatClassImageResolver.getBoatClassIconResource(boatclass.getName())
                                                .getSafeUri().asString()
                                        + "');\"></div>");
                    }
                }
            }) {
        @Override
        public SailorProfileEntry getValue(SailorProfileEntry object) {
            return object;
        }
    };
    private final Column<SailorProfileEntry, String> competitorColumn = new Column<SailorProfileEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            if (entry != null && entry.getCompetitors() != null) {
                return entry.getCompetitors().stream().map(c -> c.getName()).collect(Collectors.joining(", "));
            }
            return "-";
        }
    };
    private final Column<SailorProfileEntry, String> showColumn = new Column<SailorProfileEntry, String>(
            new ButtonCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            return ">";
        }
    };

    @Override
    public NeedsAuthenticationContext getAuthenticationContext() {
        return decoratorUi;
    }
}
