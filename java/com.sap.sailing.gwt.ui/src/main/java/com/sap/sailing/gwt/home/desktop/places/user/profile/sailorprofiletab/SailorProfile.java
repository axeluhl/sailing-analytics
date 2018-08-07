package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.BadgeDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SailorProfileEntry;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfile;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfile extends Composite implements SailorProfileView {

    interface MyBinder extends UiBinder<Widget, SailorProfile> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true)
    SharedSailorProfile sharedSailorProfileUi;
    @UiField(provided = true)
    final SortedCellTable<SailorProfileEntry> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    private final FlagImageResolver flagImageResolver;

    public SailorProfile(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        sharedSailorProfileUi = new SharedSailorProfile(presenter.getSharedSailorProfilePresenter(), flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        setupTable();
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
            public void update(int index, SailorProfileEntry object, String value) {
                Window.alert("on-goto-click");
            }
        });
    }

    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }

    @Override
    public void setProfileList(List<SailorProfileEntry> entries) {
        sailorProfilesTable.setPageSize(entries.size());
        sailorProfilesTable.setList(entries);
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
                StringBuilder sb = new StringBuilder();
                for (BadgeDTO badge : entry.getBadges()) {
                    sb.append(badge.getName());
                    sb.append(" ");
                }
                return sb.toString();
            }
            return "-";
        }
    };
    private final Column<SailorProfileEntry, String> boatClassColumn = new Column<SailorProfileEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            if (entry != null && entry.getBoatclasses() != null) {
                StringBuilder sb = new StringBuilder();
                for (BoatClassDTO boatclass : entry.getBoatclasses()) {
                    sb.append(boatclass.getName());
                    sb.append(" ");
                }
                return sb.toString();
            }
            return "-";
        }
    };
    private final Column<SailorProfileEntry, String> competitorColumn = new Column<SailorProfileEntry, String>(
            new TextCell()) {
        @Override
        public String getValue(SailorProfileEntry entry) {
            if (entry != null && entry.getCompetitors() != null) {
                StringBuilder sb = new StringBuilder();
                for (CompetitorDTO competitor : entry.getCompetitors()) {
                    sb.append(competitor.getName());
                    sb.append(" ");
                }
                return sb.toString();
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
}